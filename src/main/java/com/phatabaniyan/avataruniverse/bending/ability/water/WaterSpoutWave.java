package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code WaterSpoutWave} with the reference
 * implementation's staging: CLICK holds a water source (smoke puff),
 * Sneak charges a torrent-style ring in place (no travelling block), and
 * release surfs a decaying ride (speed 1.3 fading over 2.5s) laying a
 * melting water trail. Re-sneak ends the ride with a shared-bucket cooldown;
 * tap-sneaks and early removals are free. Shares the "WaterSpout" bind and
 * cooldown bucket; mutual exclusion with the spout both ways.
 *
 * <p>Trail and ring blocks are quiet {@link TempBlock}s with timed reverts;
 * all revert on removal.</p>
 */
public class WaterSpoutWave extends BendingAbility {
    public static final String ID = "WaterSpoutWave";
    /** Shared bind/cooldown bucket name (reference parity). */
    public static final String BIND_ID = "WaterSpout";

    private enum Stage {
        CLICK,
        SHIFT,
        RELEASE
    }

    private enum AnimateState {
        CIRCLE,
        SHRINK
    }

    private static final double SELECT_RANGE = 6.0;
    private static final double RADIUS = 3.8;
    private static final double WAVE_RADIUS = 1.5;
    private static final long CHARGE_TIME_MS = 500L;
    private static final long FLIGHT_DURATION_MS = 2500L;
    private static final double SPEED = 1.3;
    private static final int COOLDOWN_TICKS = 120;
    private static final int TRAIL_REVERT_TICKS = 20;
    private static final long SMOKE_INTERVAL_MS = 100L;

    private final ServerLevel level;
    private Stage type = Stage.CLICK;
    private AnimateState animation;
    private boolean charging;
    private boolean moving;
    private BlockPos origin;
    private Vec3 location = Vec3.ZERO;
    private Vec3 direction = new Vec3(1.0, 0.0, 0.0);
    private double radius = RADIUS;
    private long rideStartTime;
    private long chargeStartTime;
    private long lastSmokeTime;
    private final Map<BlockPos, TempBlock> affectedBlocks = new HashMap<>();
    /** Icy origin (Korra IceWave): ice trail + contact damage + trapping spheres. */
    private boolean icy;

    private final Set<UUID> icedEntities = new HashSet<>();

    private record FrozenEntry(UUID owner, long revertAt) {}

    private static final Map<TempBlock, FrozenEntry> FROZEN = new ConcurrentHashMap<>();
    private static final double CLEANUP_RANGE_SQR = 10.0 * 10.0;

    public WaterSpoutWave(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.origin = raycastWaterSource(player, SELECT_RANGE);
        if (this.origin != null) {
            this.location = Vec3.atCenterOf(this.origin);
        }
    }

    /** Factory: null when no source in range (cast fizzles like Korra). */
    public static WaterSpoutWave create(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WaterSpoutWave wave = new WaterSpoutWave(player);
        if (wave.origin == null || !BendingSources.isWaterSource(level, wave.origin)) {
            return null;
        }
        wave.icy = BendingSources.isIce(level, wave.origin);
        return wave;
    }

    /**
     * Factory from an already-validated tapped position (tap-to-select flow):
     * the eye-ray may miss what the tap found (different reach), so the tap
     * carries its own origin instead of fizzling.
     */
    public static WaterSpoutWave createAt(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!BendingSources.isWaterSource(level, pos)) {
            return null;
        }
        WaterSpoutWave wave = new WaterSpoutWave(player);
        wave.origin = pos.immutable();
        wave.location = Vec3.atCenterOf(wave.origin);
        wave.icy = BendingSources.isIce(player.serverLevel(), wave.origin);
        return wave;
    }

    @Override
    public String name() {
        return ID;
    }

    /** Eye-ray water source within range (march 0.5 blocks, Korra BlockSource). */
    public static BlockPos raycastWaterSource(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (BendingSources.isWaterSource(level, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** Gaze target point: eye-ray march stopping at the first solid block. */
    public static Vec3 gazeTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        Vec3 last = eye;
        for (double d = 0.5; d <= range; d += 0.5) {
            Vec3 at = eye.add(look.scale(d));
            if (!BendingSources.isTransparentForBend(level, BlockPos.containing(at))) {
                break;
            }
            last = at;
        }
        return last;
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
        // Mutual exclusion (Korra: wave kills spout every tick).
        WaterSpout spout = BendingManager.find(owner, WaterSpout.class);
        if (spout != null) {
            BendingManager.remove(spout);
        }
        switch (type) {
            case CLICK -> {
                if (!boundTo(player, bending)) {
                    return false;
                }
                return progressClick(player);
            }
            case SHIFT -> {
                if (!boundTo(player, bending)) {
                    return false;
                }
                return progressShift(player);
            }
            case RELEASE -> {
                return progressRelease(player, bending);
            }
        }
        return true;
    }

    private boolean boundTo(ServerPlayer player, BendingPlayer bending) {
        return BIND_ID.equalsIgnoreCase(bending.boundAbility(player.getInventory().selected + 1));
    }

    private boolean progressClick(ServerPlayer player) {
        // Leashed to the tapped origin, not the gaze: the player must be free
        // to look around (and at the horizon to aim the ride) while holding
        // the source. Dies only when they walk away or the source is gone.
        if (player.blockPosition().distSqr(origin) > SELECT_RANGE * SELECT_RANGE) {
            return false;
        }
        if (!BendingSources.isWaterSource(level, origin)) {
            return false;
        }
        if (player.isShiftKeyDown()) {
            type = Stage.SHIFT;
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - lastSmokeTime >= SMOKE_INTERVAL_MS) {
            lastSmokeTime = now;
            Vec3 c = Vec3.atCenterOf(origin);
            level.sendParticles(
                    BendingTheme.particle(Config.WATERSPOUTWAVE_TRAIL_PARTICLE.get(), ParticleTypes.SMOKE),
                    c.x,
                    c.y + 0.5,
                    c.z,
                    Config.WATERSPOUTWAVE_TRAIL_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.02);
        }
        return true;
    }

    private boolean progressShift(ServerPlayer player) {
        if (!charging) {
            charging = true;
            animation = AnimateState.CIRCLE;
            chargeStartTime = System.currentTimeMillis();
        }
        if (!player.isShiftKeyDown()) {
            if (System.currentTimeMillis() - chargeStartTime >= CHARGE_TIME_MS) {
                type = Stage.RELEASE;
                animation = AnimateState.SHRINK;
                return true;
            }
            return false;
        }
        // Torrent-style ring: flat spinning arc around the player from the
        // first sneak tick — no travelling block, nothing climbs skyward.
        drawCircle(220.0, 5.0, player);
        return true;
    }

    private boolean progressRelease(ServerPlayer player, BendingPlayer bending) {
        if (animation == AnimateState.SHRINK) {
            radius -= 0.20;
            drawCircle(360.0, 15.0, player);
            if (radius < 1.0) {
                revertBlocks();
                animation = null;
                moving = true;
                rideStartTime = System.currentTimeMillis();
            }
            return true;
        }
        if (!moving) {
            moving = true;
            rideStartTime = System.currentTimeMillis();
        }
        long elapsed = System.currentTimeMillis() - rideStartTime;
        if (elapsed > FLIGHT_DURATION_MS || player.isShiftKeyDown()) {
            if (elapsed >= CHARGE_TIME_MS) {
                bending.setCooldown(BIND_ID, player.level().getGameTime() + COOLDOWN_TICKS);
            }
            return false;
        }
        player.fallDistance = 0.0F;
        double speed = SPEED * (1.0 - (double) elapsed / FLIGHT_DURATION_MS);
        Vec3 push = player.getLookAngle().normalize().scale(Math.max(0.0, speed));
        player.setDeltaMovement(push);
        player.hurtMarked = true;
        // Bukkit-parity trail: 3D sphere around exact feet-1, air cells only.
        // Icy origins (Korra IceWave) lay ice instead, with contact damage.
        for (BlockPos pos : trailFootprint(player.position())) {
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            if (icy) {
                TempBlock existing = affectedBlocks.get(pos);
                if (existing != null) {
                    continue;
                }
                TempBlock ice = new TempBlock(level, pos, Blocks.PACKED_ICE.defaultBlockState());
                affectedBlocks.put(pos, ice);
                BendingManager.scheduleRevert(ice, level.getGameTime() + Config.ICEWAVE_REVERT_SECONDS.get() * 20L);
            } else {
                setTrailBlock(pos);
            }
        }
        if (icy) {
            AABB box = player.getBoundingBox().inflate(2.0);
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class, box, e -> !e.getUUID().equals(owner) && e.isAlive())) {
                if (!icedEntities.add(entity.getUUID())) {
                    continue;
                }
                entity.hurtMarked = true;
                entity.invulnerableTime = 0;
                entity.hurt(
                        level.damageSources().magic(),
                        Config.ICEWAVE_DAMAGE.get().floatValue());
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 150, 2));
                encase(entity);
            }
        }
        return true;
    }

    /** Encase a victim in a thawing ice sphere (Korra IceWave spheres). */
    private void encase(LivingEntity victim) {
        long revertAt = level.getGameTime() + Config.ICEWAVE_REVERT_SECONDS.get() * 20L;
        Vec3 center = victim.position();
        double radius = Config.ICEWAVE_SPHERE_RADIUS.get();
        int bound = (int) Math.ceil(radius);
        BlockPos centerBlock = BlockPos.containing(center);
        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    BlockPos pos = centerBlock.offset(ox, oy, oz);
                    double dx = pos.getX() + 0.5 - center.x;
                    double dy = pos.getY() + 0.5 - center.y;
                    double dz = pos.getZ() + 0.5 - center.z;
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    var state = level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.ICE) || BendingSources.isWaterSource(level, pos)) {
                        TempBlock ice = new TempBlock(level, pos.immutable(), Blocks.ICE.defaultBlockState());
                        FROZEN.put(ice, new FrozenEntry(owner, revertAt));
                    }
                }
            }
        }
    }

    /** Trail footprint around exact feet-1: 3D sphere hugging slopes like Korra. */
    public static Set<BlockPos> trailFootprint(Vec3 feetPos) {
        Set<BlockPos> out = new HashSet<>();
        Vec3 center = feetPos.add(0.0, -1.0, 0.0);
        int r = (int) Math.ceil(WAVE_RADIUS);
        BlockPos centerBlock = BlockPos.containing(center);
        for (int ox = -r; ox <= r; ox++) {
            for (int oy = -r; oy <= r; oy++) {
                for (int oz = -r; oz <= r; oz++) {
                    BlockPos pos = centerBlock.offset(ox, oy, oz);
                    double dx = pos.getX() + 0.5 - center.x;
                    double dy = pos.getY() + 0.5 - center.y;
                    double dz = pos.getZ() + 0.5 - center.z;
                    if (dx * dx + dy * dy + dz * dz > WAVE_RADIUS * WAVE_RADIUS) {
                        continue;
                    }
                    out.add(pos.immutable());
                }
            }
        }
        return out;
    }

    private void drawCircle(double thetaDegrees, double increment, ServerPlayer player) {
        // Same spin as the torrent ring (20°/tick).
        direction = rotateXZ(direction, Math.toRadians(20.0));
        // Ring centers on the live eye position every tick (never frozen).
        Vec3 eye = player.getEyePosition();
        Vec3 center = new Vec3(eye.x, Math.floor(eye.y), eye.z);
        Set<BlockPos> want = new HashSet<>();
        for (double i = 0.0; i <= thetaDegrees; i += increment) {
            double angle = Math.toRadians(i - thetaDegrees / 2.0);
            Vec3 off = rotateXZ(direction, angle).normalize().scale(radius);
            Vec3 dir = new Vec3(off.x, 0.0, off.z);
            BlockPos pos = BlockPos.containing(center.add(dir));
            // Transparent gate like the torrent ring (NOT air-only): the wave
            // charges at the water's edge, so air-only punches rotating holes
            // over every lake cell.
            if (BendingSources.isTransparentForBend(level, pos)) {
                want.add(pos.immutable());
            }
        }
        // Diff-update (not revert-all): held cells persist across ticks, so
        // the spinning ring reads solid instead of strobing.
        for (Map.Entry<BlockPos, TempBlock> entry : new java.util.ArrayList<>(affectedBlocks.entrySet())) {
            if (!want.contains(entry.getKey())) {
                entry.getValue().revert();
                affectedBlocks.remove(entry.getKey());
            }
        }
        for (BlockPos pos : want) {
            if (!affectedBlocks.containsKey(pos)) {
                setBlock(pos);
            }
        }
        location = center;
    }

    private static Vec3 rotateXZ(Vec3 vec, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vec.x * cos - vec.z * sin, vec.y, vec.x * sin + vec.z * cos);
    }

    private void setBlock(BlockPos pos) {
        TempBlock existing = affectedBlocks.get(pos);
        if (existing != null) {
            existing.revert();
        }
        affectedBlocks.put(pos, new TempBlock(level, pos, Blocks.WATER.defaultBlockState(), TempBlock.QUIET));
    }

    private void setTrailBlock(BlockPos pos) {
        TempBlock existing = affectedBlocks.get(pos);
        if (existing != null) {
            return;
        }
        TempBlock temp = new TempBlock(level, pos, Blocks.WATER.defaultBlockState(), TempBlock.QUIET);
        affectedBlocks.put(pos, temp);
        BendingManager.scheduleRevert(temp, level.getGameTime() + TRAIL_REVERT_TICKS);
    }

    private void revertBlocks() {
        for (TempBlock temp : new java.util.ArrayList<>(affectedBlocks.values())) {
            temp.revert();
        }
        affectedBlocks.clear();
    }

    /**
     * Thaw wave ice spheres whose timer expired, whose owner logged off or
     * changed dimension, or that drifted beyond thaw range (Korra
     * progressAllCleanup).
     */
    public static void tickFrozen(MinecraftServer server) {
        if (FROZEN.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        double thawRangeSqr = Config.ICEWAVE_THAW_RADIUS.get() * Config.ICEWAVE_THAW_RADIUS.get();
        FROZEN.entrySet().removeIf(entry -> {
            TempBlock block = entry.getKey();
            FrozenEntry record = entry.getValue();
            ServerPlayer owner = server.getPlayerList().getPlayer(record.owner());
            if (owner == null || now >= record.revertAt()) {
                block.revert();
                return true;
            }
            if (!owner.serverLevel().equals(block.level())
                    || owner.blockPosition().distSqr(block.pos()) > thawRangeSqr) {
                block.revert();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onRemove() {
        revertBlocks();
    }
}
