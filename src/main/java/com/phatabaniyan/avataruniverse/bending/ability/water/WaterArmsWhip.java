package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code WaterArmsWhip} (Pull/Punch/Grapple/Grab). A whip
 * extends from the active arm tip along the look direction, growing 1 block
 * per tick and retracting past full length; blocked cells latch (grapple) or
 * trigger the mode action. Grab latches one victim (registry + timeout +
 * damage-release) and re-cast throws it. Per-arm locks and per-mode
 * cooldowns mirror Korra; night scaling, regions and permissions are out of
 * scope.
 */
public class WaterArmsWhip extends BendingAbility {
    public static final String ID = "WaterArmsWhip";

    public enum WhipMode {
        PULL,
        PUNCH,
        GRAPPLE,
        GRAB
    }

    private record GrabRecord(UUID ownerUuid, long expiresAtTick, float ownerHealthSnap) {}

    private static final Map<UUID, GrabRecord> GRABS = new ConcurrentHashMap<>();

    private final ServerLevel level;
    private final WhipMode mode;
    private final WaterArms.Arm arm;
    private final int whipLength;
    private final int initLength;
    private int activeLength;
    private final int whipSpeed = 1;
    private final long usageCooldownTicks;
    private boolean reverting;
    private boolean hasDamaged;
    private boolean grappled;
    private boolean grabbed;
    private UUID grabbedVictim;
    private long grabTimeoutTick;
    private float ownerHealthSnap;
    private Vec3 tip = Vec3.ZERO;
    private final List<TempBlock> trail = new ArrayList<>();

    private WaterArmsWhip(ServerPlayer player, WhipMode mode, WaterArms.Arm arm, int whipLength) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.mode = mode;
        this.arm = arm;
        this.initLength = Config.WATERARMS_INITIAL_LENGTH.get();
        this.whipLength = whipLength;
        this.activeLength = initLength;
        this.usageCooldownTicks = switch (mode) {
            case PULL -> Config.msToTicks(Config.WATERARMS_WHIP_COOLDOWN_PULL_MS.get());
            case PUNCH -> Config.msToTicks(Config.WATERARMS_WHIP_COOLDOWN_PUNCH_MS.get());
            case GRAPPLE -> Config.msToTicks(Config.WATERARMS_WHIP_COOLDOWN_GRAPPLE_MS.get());
            case GRAB -> Config.msToTicks(Config.WATERARMS_WHIP_COOLDOWN_GRAB_MS.get());};
    }

    /**
     * Start a whip, or throw a grabbed victim when re-cast while grabbing
     * (Korra: re-cast releases at 2.5x velocity, no new instance).
     * Returns the instance, or null when thrown/aborted.
     */
    public static WaterArmsWhip create(ServerPlayer player, WhipMode mode, WaterArms parent, BendingPlayer bending) {
        WaterArmsWhip old = BendingManager.find(player.getUUID(), WaterArmsWhip.class);
        if (old != null && old.grabbed) {
            LivingEntity victim =
                    old.grabbedVictim == null ? null : (LivingEntity) old.level.getEntity(old.grabbedVictim);
            if (victim != null && victim.isAlive()) {
                victim.setDeltaMovement(victim.getDeltaMovement().scale(2.5));
                victim.hurtMarked = true;
            }
            if (old.grabbedVictim != null) {
                GRABS.remove(old.grabbedVictim);
            }
            BendingManager.remove(old);
            return null;
        }
        if (old != null && old.arm != parent.getActiveArm()) {
            return null;
        }
        int length = mode == WhipMode.PUNCH
                ? Config.WATERARMS_WHIP_PUNCH_MAX_LENGTH.get()
                : Config.WATERARMS_WHIP_MAX_LENGTH.get();
        if (!parent.isFullSource()) {
            length = Config.WATERARMS_WHIP_MAX_LENGTH_WEAK.get();
        }
        // Korra picks the arm here: toggle, keep the first displayable
        // side, so a spent/locked arm is skipped for the live one.
        parent.switchPreferredArm(player);
        WaterArmsWhip whip = new WaterArmsWhip(player, mode, parent.getActiveArm(), length);
        String armKey = whip.arm == WaterArms.Arm.LEFT ? "WaterArms_LEFT" : "WaterArms_RIGHT";
        boolean armLocked = whip.arm == WaterArms.Arm.LEFT ? parent.isLeftArmCooldown() : parent.isRightArmCooldown();
        // A consumed (speared) arm is dead: only the other arm casts, never a
        // magical re-appearance from the spent one.
        if (armLocked
                || parent.isArmConsumed(whip.arm)
                || bending.isOnCooldown(armKey, player.level().getGameTime())) {
            return null;
        }
        if (Config.WATERARMS_WHIP_USAGE_COOLDOWN_ENABLED.get()) {
            bending.setCooldown(armKey, player.level().getGameTime() + whip.usageCooldownTicks);
        }
        if (whip.arm == WaterArms.Arm.LEFT) {
            parent.setLeftArmCooldown(true);
        } else {
            parent.setRightArmCooldown(true);
        }
        return whip;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isRemoved() || player.isDeadOrDying()) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null
                || !bending.hasElement(com.phatabaniyan.avataruniverse.bending.BendingElement.WATER)
                || !bending.isToggled()) {
            return false;
        }
        WaterArms parent = BendingManager.find(owner, WaterArms.class);
        if (parent == null) {
            return false;
        }
        if (activeLength < whipLength && !reverting) {
            activeLength += whipSpeed;
        } else if (activeLength > initLength) {
            if (!grabbed) {
                activeLength -= whipSpeed;
            }
        } else {
            return false;
        }
        if (activeLength >= whipLength && !grabbed) {
            reverting = true;
        }
        if (grabbed && (level.getGameTime() > grabTimeoutTick || player.getHealth() < ownerHealthSnap)) {
            grabbed = false;
            reverting = true;
        }
        useArm(player, parent);
        if (!tip.equals(Vec3.ZERO)) {
            dragEntity(player, parent);
        }
        return true;
    }

    /** Extend the whip from the active arm tip, latching on blocked cells. */
    private void useArm(ServerPlayer player, WaterArms parent) {
        Vec3 armEnd = parent.getActiveArmEnd(player);
        Vec3 dir = player.getLookAngle().normalize();
        for (TempBlock temp : new ArrayList<>(trail)) {
            temp.revert();
        }
        trail.clear();
        boolean latched = false;
        for (int i = 1; i <= activeLength; i++) {
            BlockPos pos = BlockPos.containing(armEnd.add(dir.scale(i)));
            if (!BendingSources.isTransparentForBend(level, pos)) {
                if (!level.getBlockState(pos).is(Blocks.BARRIER)) {
                    grappled = true;
                }
                reverting = true;
                performAction(player, parent, Vec3.atCenterOf(pos));
                latched = true;
                break;
            }
            trail.add(TempBlock.cube(level, pos.immutable(), 0.25F));
            performAction(player, parent, Vec3.atCenterOf(pos));
            if (i == activeLength) {
                Vec3 side = WaterArms.sideVec(player);
                Vec3 jog = arm == WaterArms.Arm.LEFT ? side.scale(-1.0) : side.scale(1.0);
                Vec3 tipPos = Vec3.atCenterOf(pos).add(jog);
                BlockPos tipCell = BlockPos.containing(tipPos);
                if (!BendingSources.isTransparentForBend(level, tipCell)) {
                    if (!level.getBlockState(tipCell).is(Blocks.BARRIER)) {
                        grappled = true;
                    }
                    reverting = true;
                    performAction(player, parent, tipPos);
                    break;
                }
                trail.add(TempBlock.cube(level, tipCell.immutable(), 0.25F));
                performAction(player, parent, tipPos);
            }
            if (latched) {
                break;
            }
        }
        // NOTE: tip stays at the last marched cell (the visual whip end).
        // Overwriting it with the short arm base here used to drag victims
        // all the way to the player instead of holding them out front.
    }

    /** Mode action at each whip cell plus the tip, every tick (Korra continuous). */
    private void performAction(ServerPlayer player, WaterArms parent, Vec3 at) {
        tip = at;
        switch (mode) {
            case PULL -> {
                Vec3 armEnd = parent.getActiveArmEnd(player);
                for (Entity entity : entitiesAround(at, 2.0)) {
                    if (entity instanceof ServerPlayer target && (target.isCreative() || target.isSpectator())) {
                        continue;
                    }
                    Vec3 pull = armEnd.subtract(entity.position()).scale(Config.WATERARMS_WHIP_PULL_MULTIPLIER.get());
                    entity.setDeltaMovement(pull);
                    entity.hurtMarked = true;
                }
            }
            case PUNCH -> {
                Vec3 armEnd = parent.getActiveArmEnd(player);
                for (Entity entity : entitiesAround(at, 2.0)) {
                    if (entity instanceof ServerPlayer target && (target.isCreative() || target.isSpectator())) {
                        continue;
                    }
                    Vec3 push = entity.position().subtract(armEnd).scale(0.15);
                    entity.setDeltaMovement(push);
                    entity.hurtMarked = true;
                    if (entity instanceof LivingEntity living
                            && !entity.getUUID().equals(owner)) {
                        hasDamaged = true;
                        living.hurt(
                                level.damageSources().playerAttack(player),
                                Config.WATERARMS_WHIP_PUNCH_DAMAGE.get().floatValue());
                    }
                }
            }
            case GRAPPLE -> grapplePlayer(player, at);
            case GRAB -> {
                if (grabbedVictim != null) {
                    return;
                }
                for (Entity entity : entitiesAround(at, 2.0)) {
                    if (!(entity instanceof LivingEntity living)
                            || entity.getUUID().equals(owner)
                            || !entity.isAlive()
                            || GRABS.containsKey(entity.getUUID())) {
                        continue;
                    }
                    GRABS.put(
                            entity.getUUID(),
                            new GrabRecord(
                                    owner,
                                    level.getGameTime()
                                            + Config.msToTicks(Config.WATERARMS_WHIP_GRAB_DURATION_MS.get()),
                                    player.getHealth()));
                    grabbedVictim = entity.getUUID();
                    grabbed = true;
                    reverting = true;
                    parent.setActiveArmCooldown(true);
                    ownerHealthSnap = player.getHealth();
                    grabTimeoutTick =
                            level.getGameTime() + Config.msToTicks(Config.WATERARMS_WHIP_GRAB_DURATION_MS.get());
                    break;
                }
            }
        }
    }

    private List<Entity> entitiesAround(Vec3 at, double radius) {
        AABB box = new AABB(at.subtract(radius, radius, radius), at.add(radius, radius, radius));
        return level.getEntitiesOfClass(Entity.class, box, Entity::isAlive);
    }

    /** Drag the latched victim toward the arm tip (Korra dragEntity). */
    private void dragEntity(ServerPlayer player, WaterArms parent) {
        if (grabbedVictim == null || !grabbed) {
            return;
        }
        LivingEntity victim = grabbedVictimTarget();
        if (victim == null
                || !victim.isAlive()
                || BendingManager.find(victim.getUUID(), WaterArmsWhip.class) instanceof WaterArmsWhip counter
                        && counter.mode == WhipMode.GRAB) {
            releaseGrab();
            return;
        }
        Vec3 victimPos = victim.position();
        Vec3 delta = tip.subtract(victimPos);
        if (tip.distanceTo(victimPos) > 0.5) {
            victim.setDeltaMovement(delta.normalize().scale(0.65));
        } else {
            victim.setDeltaMovement(Vec3.ZERO);
        }
        victim.hurtMarked = true;
        victim.fallDistance = 0.0F;
        if (victim instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    private LivingEntity grabbedVictimTarget() {
        if (grabbedVictim == null) {
            return null;
        }
        Entity entity = level.getEntity(grabbedVictim);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void releaseGrab() {
        if (grabbedVictim != null) {
            GRABS.remove(grabbedVictim);
            grabbedVictim = null;
        }
        grabbed = false;
    }

    /** Grapple: yank the bender toward the latched point (Korra grapplePlayer). */
    private void grapplePlayer(ServerPlayer player, Vec3 at) {
        if (!reverting || !grappled || mode != WhipMode.GRAPPLE) {
            return;
        }
        Vec3 pull = player.position().subtract(at).scale(-0.25);
        player.setDeltaMovement(pull);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new ArrayList<>(trail)) {
            temp.revert();
        }
        trail.clear();
        releaseGrab();
        WaterArms parent = BendingManager.find(owner, WaterArms.class);
        if (parent != null) {
            if (arm == WaterArms.Arm.LEFT) {
                parent.setLeftArmCooldown(false);
            } else {
                parent.setRightArmCooldown(false);
            }
            if (hasDamaged) {
                parent.decrementMaxPunches();
                messageMaxLeft(parent, "Punches Left: " + parent.getMaxPunches());
            } else {
                messageMaxLeft(parent, "Uses Left: " + parent.getMaxUses());
            }
            parent.decrementMaxUses();
        }
    }

    private void messageMaxLeft(WaterArms parent, String text) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(text), true);
        }
    }

    /** Tick the grab registry (logout/dead/expired cleanup, Torrent.tickFrozen pattern). */
    public static void tickGrabs(MinecraftServer server) {
        if (GRABS.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        GRABS.entrySet().removeIf(entry -> {
            UUID victimId = entry.getKey();
            GrabRecord record = entry.getValue();
            if (server.getPlayerList().getPlayer(record.ownerUuid()) == null) {
                return true;
            }
            Entity victim = null;
            for (ServerLevel level : server.getAllLevels()) {
                victim = level.getEntity(victimId);
                if (victim != null) {
                    break;
                }
            }
            if (!(victim instanceof LivingEntity living) || !living.isAlive()) {
                return true;
            }
            return now >= record.expiresAtTick();
        });
    }
}
