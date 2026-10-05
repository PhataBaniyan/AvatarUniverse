package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code Torrent} (waterbending/Torrent.java), mechanics
 * modeled on the reference implementation: click with a selected source to
 * ready, hold sneak and the consumed source travels to the ring slot, a
 * spinning arc grows around the player and deflects entities, releasing
 * sneak fires the radial burst, clicking a completed ring launches the
 * directed stream (head +1 / tail -1 stepper that shrinks to empty on
 * wall/range), clicking mid-flight freezes it into trapping ice that thaws
 * on a timer.
 *
 * <p>Kept deviations from the reference: kept freeze (reference cut it),
 * tap-to-select instead of eye-ray, live look steering, no day/night or
 * Avatar scaling, no region protection.</p>
 *
 * <p>Every block in every phase is a {@link TempBlock} and reverts.</p>
 */
public class Torrent extends BendingAbility {
    public static final String ID = "Torrent";

    public enum State {
        /** Source tapped, waiting for the player to hold sneak. */
        WAITING,
        /** Consumed source travelling to the ring slot. */
        SETTING_UP,
        FORMING,
        FORMED,
        LAUNCHING
    }

    private record FrozenEntry(UUID owner, long revertAt) {}

    private static final Map<TempBlock, FrozenEntry> FROZEN = new ConcurrentHashMap<>();
    private static final double CLEANUP_RANGE_SQR = 50.0 * 50.0;

    private final ServerLevel level;
    private final BlockPos sourcePos;
    private State state = State.WAITING;
    private double angle = 40.0;
    private double startAngle;
    private int ticks;
    private int waitTicks;
    private int formedAtTick;
    private boolean finished;
    private Vec3 setupLoc = Vec3.ZERO;
    private TempBlock setupTemp;
    private TempBlock sourceTemp;
    /** Launched arc queue (head-first). Slides +1/-1 per tick. */
    private final Deque<TempBlock> launched = new ArrayDeque<>();

    private final Set<UUID> hurtEntities = new HashSet<>();
    private Vec3 head = Vec3.ZERO;
    private Vec3 dir = new Vec3(0.0, 0.0, 1.0);
    private Vec3 launchDir = new Vec3(0.0, 0.0, 1.0);
    private double traveled;
    /** Per-tick steering limit so bends stay gradual (about 10 degrees). */
    private static final double MAX_TURN_RADIANS = 0.17;
    /** Total bend allowed away from the launch heading (about 75 degrees). */
    private static final double MAX_TOTAL_RADIANS = 1.31;

    public Torrent(ServerPlayer player, BlockPos source) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.sourcePos = source.immutable();
        level.playSound(null, source, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    @Override
    public String name() {
        return ID;
    }

    public boolean isFormed() {
        return state == State.FORMED;
    }

    public boolean isLaunching() {
        return state == State.LAUNCHING;
    }

    /**
     * Clicks fire only a completed ring; earlier clicks do nothing at all.
     */
    public boolean fireIfFormed(ServerPlayer player) {
        if (state == State.FORMED) {
            return launch(player);
        }
        return true;
    }

    @Override
    public boolean progress() {
        if (finished) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null
                || !bending.hasElement(com.phatabaniyan.avataruniverse.bending.BendingElement.WATER)
                || !bending.isToggled()) {
            return false;
        }
        ticks++;
        if (state == State.WAITING) {
            if (!player.isShiftKeyDown()) {
                waitTicks++;
                int waitLimit = Config.TORRENT_SNEAK_WAIT_TICKS.get();
                if (waitLimit > 0 && waitTicks > waitLimit) {
                    player.displayClientMessage(
                            Component.literal("Torrent fizzled - sneak sooner after readying."), true);
                    return false;
                }
                return true;
            }
            // Sneak started: consume the source and send it travelling (Korra setup).
            // A bottled source (consumed water potion) counts without a block.
            if (!BendingSources.isWaterSource(level, sourcePos) && !bending.takeBottledSource()) {
                return false;
            }
            if (BendingSources.isPlant(level, sourcePos)) {
                BendingManager.consumePlantSource(level, sourcePos, Config.WATERMANIP_PLANT_REGROW_SECONDS.get());
            } else {
                sourceTemp = new TempBlock(level, sourcePos, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
            }
            bending.clearSource();
            setupLoc = Vec3.atCenterOf(sourcePos);
            state = State.SETTING_UP;
            return true;
        }
        if (state == State.SETTING_UP) {
            // Releasing during setup fizzles without a wave.
            if (!player.isShiftKeyDown()) {
                return false;
            }
            return progressSetup(player);
        }
        if (state == State.LAUNCHING) {
            // Once loosed, the wave flies on its own - no sneak needed.
            return advanceWave(player);
        }
        // FORMING / FORMED: letting go with a completed ring launches the
        // radial wave; letting go early fizzles it.
        if (!player.isShiftKeyDown()) {
            if (state == State.FORMED) {
                if (BendingManager.find(owner, TorrentBurst.class) == null) {
                    BendingManager.start(new TorrentBurst(player));
                    player.displayClientMessage(Component.literal("Torrent burst!"), true);
                }
                return false;
            }
            if (state == State.FORMING) {
                player.displayClientMessage(
                        Component.literal("Released too early - hold sneak until the ring completes."), true);
            }
            return false;
        }
        // FORMING / FORMED: ring lives only while sneaking. Diff-updated
        // every tick (held cells never flicker), deflect stays per-tick.
        formRing(player);
        if (emptyRing) {
            // Walled in with nowhere to form.
            return false;
        }
        deflect(player);
        if (state == State.FORMING && angle >= 220.0) {
            state = State.FORMED;
            formedAtTick = ticks;
            player.displayClientMessage(
                    Component.literal("Ring complete - left-click to shoot, release for wave."), true);
        }
        if (state == State.FORMED) {
            int timeout = Config.TORRENT_FORMED_TIMEOUT_TICKS.get();
            if (timeout > 0 && ticks - formedAtTick > timeout) {
                player.displayClientMessage(Component.literal("Torrent fizzled (ring timed out)."), true);
                return false;
            }
        }
        return true;
    }

    /**
     * Bukkit settingUp: the consumed source travels 1 block/tick toward the
     * ring slot (eye + radius in facing direction at eye height). Dies on a
     * blocked path or out of range; arrival starts forming.
     */
    private boolean progressSetup(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 facing = new Vec3(look.x, 0.0, look.z);
        if (facing.lengthSqr() < 1.0e-6) {
            facing = new Vec3(0.0, 0.0, 1.0);
        }
        facing = facing.normalize();
        double radius = Config.TORRENT_RADIUS.get();
        Vec3 target = new Vec3(
                player.getX() + facing.x * radius, player.getEyePosition().y, player.getZ() + facing.z * radius);
        Vec3 to = target.subtract(setupLoc);
        if (to.lengthSqr() <= 1.0) {
            state = State.FORMING;
            revertSetup();
            return true;
        }
        if (to.lengthSqr() > Config.TORRENT_RANGE.get() * Config.TORRENT_RANGE.get()) {
            return false;
        }
        setupLoc = setupLoc.add(to.normalize());
        BlockPos pos = BlockPos.containing(setupLoc);
        if (!BendingSources.isTransparentForBend(level, pos)) {
            return false;
        }
        revertSetup();
        // Water-on-water is virtual (no placement, no flow risk), so the
        // traveller stays visible while crossing lakes.
        setupTemp = new TempBlock(level, pos.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET);
        return true;
    }

    private void revertSetup() {
        if (setupTemp != null) {
            setupTemp.revert();
            setupTemp = null;
        }
    }

    /**
     * Growing 220° arc at eye level that keeps slowly rotating. Diff-updated:
     * only cells entering or leaving the arc are touched, so held cells never
     * flicker (reference parity without the strobe).
     */
    private void formRing(ServerPlayer player) {
        startAngle = (startAngle + 20.0) % 360.0;
        Vec3 eye = player.getEyePosition();
        double radius = Config.TORRENT_RADIUS.get();
        // Sample densely enough that the arc stays contiguous even at large
        // configured radii (a 10° lattice leaves holes past radius ~5).
        double step = Math.min(10.0, Math.toDegrees(0.75 / Math.max(1.0, radius)));
        Set<BlockPos> want = new HashSet<>();
        for (double theta = startAngle; theta < startAngle + angle; theta += step) {
            double phi = Math.toRadians(theta);
            BlockPos pos = BlockPos.containing(eye.x + Math.cos(phi) * radius, eye.y, eye.z + Math.sin(phi) * radius);
            if (BendingSources.isTransparentForBend(level, pos)) {
                want.add(pos.immutable());
            }
        }
        for (TempBlock temp : new java.util.ArrayList<>(ring)) {
            if (!want.contains(temp.pos())) {
                temp.revert();
                ring.remove(temp);
            }
        }
        for (BlockPos pos : want) {
            if (!hasRingCell(pos)) {
                ring.add(new TempBlock(level, pos, Blocks.WATER.defaultBlockState(), TempBlock.QUIET));
            }
        }
        if (angle < 220.0) {
            angle += 20.0;
        }
        if (ring.isEmpty()) {
            // Walled in with nowhere to form.
            emptyRing = true;
        }
    }

    private boolean hasRingCell(BlockPos pos) {
        for (TempBlock temp : ring) {
            if (temp.pos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private final java.util.List<TempBlock> ring = new java.util.ArrayList<>();
    private boolean emptyRing;

    private void revertRing() {
        for (TempBlock temp : ring) {
            temp.revert();
        }
        ring.clear();
        emptyRing = false;
    }

    /**
     * Ring defense: tangential fling (radial rotated 50°, Y preserved) with
     * deflect damage from the player's attack, near the ring itself.
     */
    private void deflect(ServerPlayer player) {
        Vec3 center = player.getEyePosition();
        double cos = Math.cos(Math.toRadians(50.0));
        double sin = Math.sin(Math.toRadians(50.0));
        double radius = Config.TORRENT_RADIUS.get();
        AABB box = player.getBoundingBox().inflate(radius + 2.0);
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, box, e -> !e.getUUID().equals(owner) && e.isAlive())) {
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(dist - radius) > 1.5 || dist < 1.0e-6) {
                continue;
            }
            double tx = (dx * cos - dz * sin) / dist * Config.TORRENT_KNOCKBACK.get();
            double tz = (dx * sin + dz * cos) / dist * Config.TORRENT_KNOCKBACK.get();
            entity.setDeltaMovement(tx, entity.getDeltaMovement().y, tz);
            entity.fallDistance = 0.0F;
            entity.hurtMarked = true;
            entity.hurt(
                    level.damageSources().playerAttack(player),
                    Config.TORRENT_DEFLECT_DAMAGE.get().floatValue());
        }
    }

    /**
     * Snapshot the live arc into the ordered launch queue (transparent
     * segments are skipped, never breaking the whole launch). Head is the
     * first cell. Returns false when nothing launchable exists.
     */
    public boolean launch(ServerPlayer player) {
        if (ring.isEmpty()) {
            return false;
        }
        // Adopt the live ring cells (Korra): the arc flies as-is instead of
        // blinking out for a rebuild tick.
        launched.addAll(ring);
        ring.clear();
        head = Vec3.atCenterOf(launched.getFirst().pos());
        traveled = 0.0;
        state = State.LAUNCHING;
        // Aim-locked flight: the wave leaves along the full 3D look vector
        // (up 30° flies up 30°, down 30° dives 30°), then bends toward the
        // live aim only within limits — no target lock, no boomerangs.
        dir = player.getLookAngle().normalize();
        launchDir = dir;
        level.playSound(null, BlockPos.containing(head), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.8F, 0.9F);
        return true;
    }

    /** Rotate current toward target by at most maxRadians (smooth steering). */
    private static Vec3 turnToward(Vec3 current, Vec3 target, double maxRadians) {
        double dot = Math.max(-1.0, Math.min(1.0, current.dot(target)));
        double angle = Math.acos(dot);
        if (angle <= maxRadians || angle < 1.0e-6) {
            return target;
        }
        double t = maxRadians / angle;
        return current.scale(1.0 - t).add(target.scale(t)).normalize();
    }

    private static double angleBetween(Vec3 a, Vec3 b) {
        return Math.acos(Math.max(-1.0, Math.min(1.0, a.dot(b))));
    }

    /**
     * Drive the stream: fly the launch aim, bending toward the live aim only
     * within limits (about 10°/tick, 75° total), so up-30° flies up 30° and
     * down-30° dives 30°, symmetric. Extend the head one cell, drain the tail
     * one cell (shrink-to-empty on wall/range kills the stream). Damage along
     * every carried cell, single hit each.
     */
    private boolean advanceWave(ServerPlayer player) {
        traveled += 1.0;
        Vec3 eye = player.getEyePosition();
        dir = turnToward(dir, player.getLookAngle().normalize(), MAX_TURN_RADIANS);
        double total = angleBetween(dir, launchDir);
        if (total > MAX_TOTAL_RADIANS) {
            double t = (total - MAX_TOTAL_RADIANS) / total;
            dir = dir.scale(1.0 - t).add(launchDir.scale(t)).normalize();
        }
        Vec3 nextHead = head.add(dir);
        BlockPos nextPos = BlockPos.containing(nextHead);
        var nextState = level.getBlockState(nextPos);
        boolean outOfRange = nextHead.distanceTo(eye) > Config.TORRENT_RANGE.get();
        boolean blocked = !nextState.isAir() && nextState.getFluidState().isEmpty();
        if (blocked && !outOfRange) {
            // Climb over low obstructions instead of dying on contact.
            for (int i = 1; i <= Config.TORRENT_MAX_LAYER.get(); i++) {
                Vec3 risen = nextHead.add(0.0, i, 0.0);
                BlockPos risenPos = BlockPos.containing(risen);
                var risenState = level.getBlockState(risenPos);
                if (risenState.isAir() || !risenState.getFluidState().isEmpty()) {
                    nextHead = risen;
                    nextPos = risenPos;
                    nextState = risenState;
                    blocked = false;
                    break;
                }
            }
        }
        if (blocked && !outOfRange && dir.y < -0.2) {
            // Diving into terrain: skim along the surface instead of burrowing
            // in and dying, so downward shots visibly travel.
            Vec3 skim = new Vec3(dir.x, 0.0, dir.z);
            if (skim.lengthSqr() > 1.0e-4) {
                skim = skim.normalize();
                Vec3 skimHead = head.add(skim);
                BlockPos skimPos = BlockPos.containing(skimHead);
                var skimState = level.getBlockState(skimPos);
                if (skimState.isAir() || !skimState.getFluidState().isEmpty()) {
                    dir = skim;
                    nextHead = skimHead;
                    nextPos = skimPos;
                    nextState = skimState;
                    blocked = false;
                }
            }
        }
        // Tail drains every tick (revert-behind, like the wave trail).
        if (!launched.isEmpty()) {
            launched.removeLast().revert();
        }
        if (!blocked && !outOfRange) {
            head = nextHead;
            // Water pass-through: head advances, queue holds length (virtual
            // TempBlocks make over-water placement free and flow-safe).
            launched.addFirst(
                    new TempBlock(level, nextPos.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET));
        }
        if (launched.isEmpty()) {
            return false;
        }
        double knockback = Config.TORRENT_KNOCKBACK.get();
        double knockup = Config.TORRENT_KNOCKUP.get();
        for (TempBlock cell : Set.copyOf(launched)) {
            AABB box = new AABB(cell.pos()).inflate(1.5);
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class, box, e -> !e.getUUID().equals(owner) && e.isAlive())) {
                if (!hurtEntities.add(entity.getUUID())) {
                    continue;
                }
                Vec3 push = new Vec3(dir.x * knockback, Math.min(dir.y * knockback, knockup), dir.z * knockback);
                entity.setDeltaMovement(push);
                entity.hurtMarked = true;
                entity.invulnerableTime = 0;
                entity.hurt(
                        level.damageSources().playerAttack(player),
                        Config.TORRENT_DAMAGE.get().floatValue());
            }
        }
        if (((int) traveled & 1) == 0) {
            Vec3 c = Vec3.atCenterOf(BlockPos.containing(head)).add(0.0, 1.0, 0.0);
            level.sendParticles(
                    BendingTheme.particle(Config.TORRENT_WAVE_PARTICLE.get(), ParticleTypes.SPLASH),
                    c.x,
                    c.y,
                    c.z,
                    Config.TORRENT_WAVE_PARTICLE_COUNT.get(),
                    0.6,
                    0.5,
                    0.6,
                    0.05);
        }
        return true;
    }

    /**
     * Freeze the travelling wave: the carried arc becomes packed ice that
     * persists on a revert timer while nearby entities are trapped (motion
     * zeroed + slowness) and damaged. Needs a committed wave (a few blocks
     * of travel) — freezing thin air is rejected. Ends the ability; the ice
     * is owned by the frozen registry, not the ability.
     */
    public boolean freeze() {
        if (traveled < 3.0 || launched.isEmpty()) {
            return false;
        }
        long revertAt = level.getGameTime() + Config.TORRENT_FROZEN_REVERT_SECONDS.get() * 20L;
        for (TempBlock temp : launched) {
            // Freeze in place (keeps the quiet flag: no neighbor wake, no
            // flow kick like a fresh placement would cause).
            temp.updateReplacement(Blocks.PACKED_ICE.defaultBlockState());
            FROZEN.put(temp, new FrozenEntry(owner, revertAt));
        }
        launched.clear();
        double freezeRadius = Config.TORRENT_FREEZE_RADIUS.get();
        Vec3 c = head;
        AABB box = new AABB(
                c.subtract(freezeRadius, freezeRadius, freezeRadius), c.add(freezeRadius, freezeRadius, freezeRadius));
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, box, e -> !e.getUUID().equals(owner))) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hurtMarked = true;
            entity.invulnerableTime = 0;
            entity.hurt(
                    level.damageSources().magic(), Config.TORRENT_DAMAGE.get().floatValue());
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 150, 2));
        }
        level.sendParticles(
                BendingTheme.particle(Config.TORRENT_FREEZE_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                c.x,
                c.y,
                c.z,
                Config.TORRENT_FREEZE_PARTICLE_COUNT.get(),
                1.0,
                1.0,
                1.0,
                0.05);
        level.playSound(null, BlockPos.containing(head), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.8F, 0.5F);
        finished = true;
        return true;
    }

    @Override
    public void onRemove() {
        revertRing();
        for (TempBlock temp : Set.copyOf(launched)) {
            temp.revert();
        }
        launched.clear();
        revertSetup();
        if (sourceTemp != null) {
            sourceTemp.revert();
            sourceTemp = null;
        }
    }

    /**
     * Thaw frozen torrents whose timer expired, whose owner logged off or
     * changed dimension, or that drifted beyond cleanup range (Korra
     * progressAllCleanup, CLEANUP_RANGE 50).
     */
    public static void tickFrozen(MinecraftServer server) {
        if (FROZEN.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        FROZEN.entrySet().removeIf(entry -> {
            TempBlock block = entry.getKey();
            FrozenEntry record = entry.getValue();
            ServerPlayer owner = server.getPlayerList().getPlayer(record.owner());
            if (owner == null || now >= record.revertAt()) {
                block.revert();
                return true;
            }
            if (!owner.serverLevel().equals(block.level())
                    || owner.blockPosition().distSqr(block.pos()) > CLEANUP_RANGE_SQR) {
                block.revert();
                return true;
            }
            return false;
        });
    }
}
