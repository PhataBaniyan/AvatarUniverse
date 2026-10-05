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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code WaterArmsSpear}: consumes an arm to fire a
 * water spear 2 blocks per tick; on entity hit it damages (if enabled) and
 * encases the victim in a timed ice ball; on a wall it converts its trail
 * into a timed ice spear. Night scaling, regions and permissions are out of
 * scope.
 */
public class WaterArmsSpear extends BendingAbility {
    public static final String ID = "WaterArmsSpear";

    private record FrozenEntry(UUID owner, long revertAt) {}

    private static final Map<TempBlock, FrozenEntry> ICE_BLOCKS = new ConcurrentHashMap<>();
    private static final double CLEANUP_RANGE_SQR = 50.0 * 50.0;

    private final ServerLevel level;
    private final WaterArms.Arm arm;
    private final boolean canFreeze;
    private final Vec3 direction;
    private Vec3 position;
    private double traveled;
    private boolean hitEntity;
    private final List<TempBlock> trail = new ArrayList<>();
    private final List<BlockPos> path = new ArrayList<>();

    private WaterArmsSpear(ServerPlayer player, WaterArms.Arm arm, Vec3 direction, Vec3 origin, boolean canFreeze) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.arm = arm;
        this.direction = direction;
        this.position = origin;
        this.traveled = 0.0;
        this.hitEntity = false;
        this.canFreeze = canFreeze;
    }

    /**
     * Fire from the preferred displayable arm, consuming it (Korra
     * createInstance). Returns null when no arm is available or locked.
     */
    public static WaterArmsSpear create(
            ServerPlayer player, boolean canFreeze, WaterArms parent, BendingPlayer bending) {
        parent.switchPreferredArm(player);
        WaterArms.Arm arm = parent.getActiveArm();
        if (parent.isArmConsumed(arm)) {
            return null;
        }
        String armKey = arm == WaterArms.Arm.LEFT ? "WaterArms_LEFT" : "WaterArms_RIGHT";
        boolean locked = arm == WaterArms.Arm.LEFT ? parent.isLeftArmCooldown() : parent.isRightArmCooldown();
        if (locked || bending.isOnCooldown(armKey, player.level().getGameTime())) {
            return null;
        }
        if (!parent.canDisplayActiveArm(player)) {
            return null;
        }
        if (Config.WATERARMS_WHIP_USAGE_COOLDOWN_ENABLED.get()) {
            bending.setCooldown(armKey, player.level().getGameTime() + Config.WATERARMS_SPEAR_COOLDOWN_TICKS.get());
        }
        parent.consumeArm(arm);
        if (arm == WaterArms.Arm.LEFT) {
            parent.setLeftArmCooldown(true);
        } else {
            parent.setRightArmCooldown(true);
        }
        Vec3 origin = parent.getActiveArmEnd(player).add(player.getLookAngle().normalize());
        Vec3 target = WaterArms.aimedPoint(player, Config.WATERARMS_SPEAR_RANGE.get());
        Vec3 dir = target.subtract(origin);
        if (dir.lengthSqr() < 1.0e-4) {
            dir = player.getLookAngle().normalize();
        } else {
            dir = dir.normalize();
        }
        return new WaterArmsSpear(player, arm, dir, origin, canFreeze);
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
        if (traveled > Config.WATERARMS_SPEAR_RANGE.get()) {
            return false;
        }
        if (!hitEntity) {
            progressSpear(player);
        } else {
            createIceBall();
            return false;
        }
        BlockPos current = BlockPos.containing(position);
        if (!BendingSources.isTransparentForBend(level, current)) {
            if (canFreeze) {
                createSpear();
            }
            return false;
        }
        return true;
    }

    /** Two collision-checked steps per tick (Korra progressSpear, no tunneling). */
    private void progressSpear(ServerPlayer player) {
        for (int i = 0; i < 2; i++) {
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(position.subtract(2.0, 2.0, 2.0), position.add(2.0, 2.0, 2.0)),
                    e -> !e.getUUID().equals(owner) && e.isAlive())) {
                hitEntity = true;
                position = entity.position();
                if (Config.WATERARMS_SPEAR_DAMAGE_ENABLED.get()) {
                    entity.hurtMarked = true;
                    entity.invulnerableTime = 0;
                    entity.hurt(
                            level.damageSources().playerAttack(player),
                            Config.WATERARMS_SPEAR_DAMAGE.get().floatValue());
                }
                return;
            }
            BlockPos current = BlockPos.containing(position);
            if (!BendingSources.isTransparentForBend(level, current)) {
                return;
            }
            TempBlock water =
                    new TempBlock(level, current.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET);
            trail.add(water);
            BendingManager.scheduleRevert(water, level.getGameTime() + 12);
            position = position.add(direction);
            path.add(BlockPos.containing(position).immutable());
            traveled += 1.0;
        }
    }

    /**
     * Convert the last spearLength trail positions to timed ice (Korra
     * createSpear). Water trail reverts; ice persists on duration + jitter.
     */
    private void createSpear() {
        for (TempBlock temp : new ArrayList<>(trail)) {
            temp.revert();
        }
        trail.clear();
        int length = Config.WATERARMS_SPEAR_LENGTH.get();
        long baseMs = Config.WATERARMS_SPEAR_DURATION_MS.get();
        for (int i = path.size() - length; i < path.size(); i++) {
            if (i < 0) {
                continue;
            }
            BlockPos pos = path.get(i);
            if (!BendingSources.isTransparentForBend(level, pos)) {
                continue;
            }
            TempBlock ice = new TempBlock(level, pos, Blocks.ICE.defaultBlockState(), TempBlock.QUIET);
            ICE_BLOCKS.put(ice, new FrozenEntry(owner, level.getGameTime() + baseMs / 50 + level.random.nextInt(10)));
        }
    }

    /** Encase the victim in a timed ice ball (Korra createIceBall). */
    private void createIceBall() {
        double radius = Config.WATERARMS_SPEAR_SPHERE_RADIUS.get();
        if (radius <= 0.0) {
            if (canFreeze) {
                createSpear();
            }
            return;
        }
        long baseMs = Config.WATERARMS_SPEAR_DURATION_MS.get();
        BlockPos centerBlock = BlockPos.containing(position);
        int bound = (int) Math.ceil(radius);
        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    BlockPos pos = centerBlock.offset(ox, oy, oz);
                    double dx = pos.getX() + 0.5 - position.x;
                    double dy = pos.getY() + 0.5 - position.y;
                    double dz = pos.getZ() + 0.5 - position.z;
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    if (!BendingSources.isTransparentForBend(level, pos)) {
                        continue;
                    }
                    TempBlock ice =
                            new TempBlock(level, pos.immutable(), Blocks.ICE.defaultBlockState(), TempBlock.QUIET);
                    ICE_BLOCKS.put(
                            ice, new FrozenEntry(owner, level.getGameTime() + baseMs / 50 + level.random.nextInt(10)));
                }
            }
        }
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new ArrayList<>(trail)) {
            temp.revert();
        }
        trail.clear();
        WaterArms parent = BendingManager.find(owner, WaterArms.class);
        if (parent != null) {
            // Korra-exact: a speared arm stays consumed AND locked. Only the
            // other arm keeps working; spending both collapses the arms.
            parent.decrementMaxUses();
        }
    }

    /** Sweep timed spear/sphere ice (logout, expiry, dimension change). */
    public static void tickFrozen(MinecraftServer server) {
        if (ICE_BLOCKS.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        ICE_BLOCKS.entrySet().removeIf(entry -> {
            TempBlock block = entry.getKey();
            FrozenEntry record = entry.getValue();
            ServerPlayer owner = server.getPlayerList().getPlayer(record.owner());
            if (owner == null || now >= record.revertAt()) {
                block.revert();
                return true;
            }
            if (!owner.serverLevel().equals(block.level())) {
                block.revert();
                return true;
            }
            return false;
        });
    }
}
