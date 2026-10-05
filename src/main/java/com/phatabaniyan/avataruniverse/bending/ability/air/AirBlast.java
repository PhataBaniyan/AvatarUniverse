package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirBlast}: sneak to fire from the eyes;
 * left-click first to select a distant origin and the blast fires from there
 * toward your cursor (self-launch included). Sneaking mid-flight steers it.
 * Reference values: Cooldown 2000ms (40 ticks), Speed 25, Range 20,
 * Radius 2, Damage 3, PushSelf 1.5, PushOthers 3, Particles 10,
 * SelectRange 10, OriginMemory 10s (200 ticks), Controllable true.
 */
public class AirBlast extends BendingAbility {
    public static final String ID = "AirBlast";

    private record OriginEntry(ServerLevel level, Vec3 pos, long expiry) {}

    private static final Map<UUID, OriginEntry> ORIGINS = new ConcurrentHashMap<>();
    private static final int MAX_TICKS = Config.AIRBLAST_MAX_TICKS.get();

    /** Reference Cooldown 2000ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.AIRBLAST_COOLDOWN_TICKS.get();
    /** Selected-origin memory 10s, in server ticks. */
    private static final int ORIGIN_TICKS = Config.AIRBLAST_ORIGIN_TICKS.get();

    private static final double SPEED = Config.AIRBLAST_SPEED.get();
    private static final double RANGE = Config.AIRBLAST_RANGE.get();
    private static final double RADIUS = Config.AIRBLAST_RADIUS.get();
    private static final double DAMAGE = Config.AIRBLAST_DAMAGE.get();
    private static final double PUSH_SELF = Config.AIRBLAST_PUSH_SELF.get();
    private static final double PUSH_OTHERS = Config.AIRBLAST_PUSH_OTHERS.get();
    private static final int PARTICLES = Config.AIRBLAST_PARTICLES.get();
    private static final boolean CONTROLLABLE = Config.AIRBLAST_CONTROLLABLE.get();
    private static final double SELECT_RANGE = Config.AIRBLAST_SELECT_RANGE.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private final boolean controllable = CONTROLLABLE;
    private boolean invalid = false;
    private int ticks = 0;
    private boolean fromOrigin = false;
    private Vec3 pos;
    private Vec3 dir;
    private Vec3 origin;
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public AirBlast(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        if (player.isEyeInFluid(FluidTags.WATER)) {
            this.invalid = true;
            return;
        }

        OriginEntry entry = ORIGINS.remove(player.getUUID());
        Vec3 eye = eye(player);
        if (entry != null && entry.level() == this.level && this.level.getGameTime() < entry.expiry()) {
            this.fromOrigin = true;
            this.origin = entry.pos();
            Vec3 look = player.getLookAngle().normalize();
            Hit hit = raycast(this.level, eye, look, RANGE);
            Vec3 target = hit.solid() == null ? hit.point() : centerOf(hit.solid());
            Vec3 d = target.subtract(this.origin);
            if (d.lengthSqr() < 1.0e-6) {
                this.invalid = true;
                return;
            }
            this.dir = d.normalize();
        } else {
            this.origin = eye;
            this.dir = player.getLookAngle().normalize();
        }
        if (!Double.isFinite(this.dir.x) || !Double.isFinite(this.dir.y) || !Double.isFinite(this.dir.z)) {
            this.invalid = true;
            return;
        }
        this.pos = this.origin;
        cool(player.getUUID(), this.level, ID, COOLDOWN_TICKS);
    }

    /** False when the constructor rejected the cast (water eyes, degenerate aim). */
    public boolean isValid() {
        return !invalid;
    }

    private static Vec3 centerOf(BlockPos p) {
        return new Vec3(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
    }

    /** Left-click with AirBlast bound: mark a distant origin. */
    public static void selectOrigin(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = eye(player);
        Vec3 look = player.getLookAngle().normalize();
        Hit hit = raycast(level, eye, look, SELECT_RANGE);
        var state = level.getBlockState(BlockPos.containing(hit.point()));
        if (!state.getFluidState().isEmpty()) {
            return;
        }
        ORIGINS.put(player.getUUID(), new OriginEntry(level, hit.point(), level.getGameTime() + ORIGIN_TICKS));
        level.playSound(
                null,
                hit.point().x,
                hit.point().y,
                hit.point().z,
                SoundEvents.BUCKET_EMPTY,
                SoundSource.PLAYERS,
                0.3F,
                1.4F);
    }

    /** Marker shimmer + expiry for selected origins. Called once per server tick. */
    public static void tickOrigins(MinecraftServer server) {
        if (ORIGINS.isEmpty()) {
            return;
        }
        ORIGINS.entrySet().removeIf(e -> {
            OriginEntry o = e.getValue();
            if (o.level().getGameTime() >= o.expiry()) {
                return true;
            }
            o.level()
                    .sendParticles(
                            BendingTheme.particle(Config.AIRBLAST_MARKER_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                            o.pos().x,
                            o.pos().y,
                            o.pos().z,
                            Config.AIRBLAST_MARKER_PARTICLE_COUNT.get(),
                            0.3,
                            0.3,
                            0.3,
                            0.02);
            return false;
        });
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (invalid) {
            return false;
        }
        if (!alive(player) || player.hasDisconnected() || !gate(owner)) {
            return false;
        }
        if (++this.ticks > MAX_TICKS) {
            return false;
        }
        double step = Math.max(0.2, SPEED / 20.0);

        if (this.controllable && player.isShiftKeyDown() && player.isAlive()) {
            Vec3 look = player.getLookAngle().normalize();
            this.dir = this.dir.scale(0.85).add(look.scale(0.15)).normalize();
        }

        int cells = Math.max(1, (int) Math.ceil(step));
        for (int i = 0; i < cells; i++) {
            Vec3 next = this.pos.add(this.dir.scale(step / cells));
            BlockPos bp = BlockPos.containing(next);
            if (!level.isLoaded(bp)) {
                return false;
            }
            var state = level.getBlockState(bp);
            if (state.is(net.minecraft.world.level.block.Blocks.FIRE)) {
                level.setBlockAndUpdate(bp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                level.playSound(
                        null, next.x, next.y, next.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.2F);
                return false;
            }
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        this.pos = this.pos.add(this.dir.scale(step));

        if (this.pos.distanceTo(this.origin) > RANGE) {
            return false;
        }

        for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(RADIUS))) {
            if (e instanceof ArmorStand) {
                continue;
            }
            boolean isUser = e.getUUID().equals(player.getUUID());
            if (isUser && !this.fromOrigin) {
                continue;
            }
            double knockback = isUser ? PUSH_SELF : PUSH_OTHERS;
            if (this.pos.distanceTo(this.origin) > 0) {
                knockback *= Math.max(0.3, 1.0 - this.pos.distanceTo(this.origin) / (2 * RANGE));
            }
            if (DAMAGE > 0 && e instanceof LivingEntity living && !isUser && this.affected.add(e.getUUID())) {
                living.hurt(player.damageSources().magic(), (float) DAMAGE);
            }
            e.setDeltaMovement(this.dir.scale(knockback));
            e.hurtMarked = true;
            if (e.isOnFire()) {
                e.clearFire();
            }
        }

        level.sendParticles(
                BendingTheme.particle(Config.AIRBLAST_TRAIL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.AIRBLAST_TRAIL_PARTICLE_COUNT.get(),
                0.275,
                0.275,
                0.275,
                0.01);
        if (player.getRandom().nextInt(4) == 0) {
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.WIND_CHARGE_BURST,
                    SoundSource.PLAYERS,
                    0.25F,
                    1.5F);
        }
        return true;
    }

    private record Hit(BlockPos solid, Vec3 point) {}

    /** March along dir; returns the first solid cell plus the last open point before it. */
    private static Hit raycast(ServerLevel level, Vec3 from, Vec3 dir, double range) {
        Vec3 point = from;
        for (double d = 0; d <= range; d += 0.5) {
            Vec3 p = new Vec3(from.x + dir.x * d, from.y + dir.y * d, from.z + dir.z * d);
            BlockPos bp = BlockPos.containing(p);
            if (!level.isLoaded(bp)) {
                return new Hit(null, point);
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp)) {
                return new Hit(bp.immutable(), point);
            }
            point = new Vec3(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
        }
        Vec3 end = new Vec3(from.x + dir.x * range, from.y + dir.y * range, from.z + dir.z * range);
        return new Hit(null, end);
    }

    private static Vec3 eye(ServerPlayer player) {
        return new Vec3(player.getX(), player.getEyeY(), player.getZ());
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
