package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
 * Port of ProjectAvatar {@code AirSwipe}: left-click for an instant fan of
 * cutting wind; hold sneak to charge it (damage and push scale up to the
 * charge factor), release to loose. Reference values: Cooldown 2000ms
 * (40 ticks), Charge 2000ms (40 ticks), Damage 3, Push 1, Speed 18,
 * Range 16, Radius 1.5, Arc 20, ArcStep 5, ChargeFactor 2, Particles 6.
 */
public class AirSwipe extends BendingAbility {
    public static final String ID = "AirSwipe";

    /** Reference Cooldown 2000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.AIRSWIPE_COOLDOWN_TICKS.get();
    /** Reference full charge 2000ms, in server ticks. */
    private static final long MAX_CHARGE_TICKS = Config.AIRSWIPE_MAX_CHARGE_TICKS.get();

    private static final double DAMAGE = Config.AIRSWIPE_DAMAGE.get();
    private static final double PUSH = Config.AIRSWIPE_PUSH.get();
    private static final double SPEED = Config.AIRSWIPE_SPEED.get();
    private static final double RANGE = Config.AIRSWIPE_RANGE.get();
    private static final double RADIUS = Config.AIRSWIPE_RADIUS.get();
    private static final int ARC = Config.AIRSWIPE_ARC.get();
    private static final int ARC_STEP = Config.AIRSWIPE_ARC_STEP.get();
    private static final double CHARGE_FACTOR = Config.AIRSWIPE_CHARGE_FACTOR.get();
    private static final int PARTICLES = Config.AIRSWIPE_PARTICLES.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private double damage = DAMAGE;
    private double push = PUSH;
    private boolean invalid = false;
    private boolean charging;
    private Vec3 origin;
    private final Map<Vec3, Vec3> streams = new ConcurrentHashMap<>();
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public AirSwipe(ServerPlayer player) {
        this(player, false);
    }

    /**
     * @param charging true when the sneak press starts a held charge
     *     (release looses it, scaling damage/push up to the charge factor);
     *     false looses an uncharged swipe immediately (click).
     */
    public AirSwipe(ServerPlayer player, boolean charging) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        // A second sneak while one is charging keeps the old charge.
        AirSwipe old = BendingManager.find(player.getUUID(), AirSwipe.class);
        if (old != null && old.charging && charging) {
            this.invalid = true;
            return;
        }

        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null && bending.isOnCooldown(ID, this.level.getGameTime())) {
            this.invalid = true;
            return;
        }
        if (player.isEyeInFluid(FluidTags.WATER)) {
            this.invalid = true;
            return;
        }
        this.charging = charging;
        if (!charging) {
            launch(player, 1.0);
        }
    }

    /** False when the constructor rejected the cast (cooldown, water eyes, kept old charge). */
    public boolean isValid() {
        return !invalid;
    }

    /** Left-click with AirSwipe bound: instant uncharged swipe. */
    public static void fire(ServerPlayer player) {
        AirSwipe swipe = new AirSwipe(player, false);
        if (swipe.isValid()) {
            BendingManager.start(swipe);
        }
    }

    private void launch(ServerPlayer player, double factor) {
        this.origin = eye(player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        for (double i = -ARC; i <= ARC; i += ARC_STEP) {
            double angle = Math.toRadians(i);
            Vec3 dir =
                    look.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).normalize();
            this.streams.put(dir, this.origin);
        }
        this.damage *= factor;
        this.push *= factor;
        this.charging = false;
        cool(player.getUUID(), this.level, ID, COOLDOWN_TICKS);
        player.serverLevel()
                .playSound(
                        null,
                        player.getX(),
                        player.getEyeY(),
                        player.getZ(),
                        SoundEvents.WIND_CHARGE_BURST,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.2F);
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
        if (this.charging) {
            if (!player.isShiftKeyDown()) {
                double elapsed = level.getGameTime() - this.startTime;
                double factor = Math.max(1.0, CHARGE_FACTOR * Math.min(1.0, elapsed / (double) MAX_CHARGE_TICKS));
                launch(player, factor);
                return true;
            }
            if (level.getGameTime() - this.startTime >= MAX_CHARGE_TICKS) {
                Vec3 eye = eye(player);
                level.sendParticles(
                        BendingTheme.particle(Config.AIRSWIPE_CHARGE_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        eye.x,
                        eye.y,
                        eye.z,
                        Config.AIRSWIPE_CHARGE_PARTICLE_COUNT.get(),
                        0.2,
                        0.2,
                        0.2,
                        0.01);
            }
            return true;
        }
        if (this.streams.isEmpty()) {
            return false;
        }
        advanceStreams(player);
        return !this.streams.isEmpty();
    }

    private void advanceStreams(ServerPlayer player) {
        double step = Math.max(0.2, SPEED / 20.0);
        for (var e : new java.util.ArrayList<>(this.streams.entrySet())) {
            Vec3 dir = e.getKey();
            Vec3 loc = e.getValue();
            int cells = Math.max(1, (int) Math.ceil(step));
            boolean dead = false;
            for (int i = 0; i < cells && !dead; i++) {
                Vec3 next = loc.add(dir.scale(step / cells));
                BlockPos bp = BlockPos.containing(next);
                if (!level.isLoaded(bp)) {
                    dead = true;
                    break;
                }
                var state = level.getBlockState(bp);
                if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                    dead = true;
                    break;
                }
                if (state.is(net.minecraft.world.level.block.Blocks.FIRE)) {
                    level.setBlockAndUpdate(bp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                } else if (state.is(net.minecraft.tags.BlockTags.REPLACEABLE)
                        && !state.is(net.minecraft.world.level.block.Blocks.SNOW)) {
                    level.setBlockAndUpdate(bp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                }
                loc = next;
            }
            if (dead || loc.distanceTo(this.origin) > RANGE) {
                this.streams.remove(dir);
                continue;
            }
            this.streams.put(dir, loc);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSWIPE_STREAM_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.AIRSWIPE_STREAM_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.01);
            if (player.getRandom().nextInt(4) == 0) {
                level.playSound(
                        null, loc.x, loc.y, loc.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.2F, 1.5F);
            }
            for (Entity entity : level.getEntities(player, new AABB(loc, loc).inflate(RADIUS))) {
                if (entity.getUUID().equals(player.getUUID()) || entity instanceof ArmorStand) {
                    continue;
                }
                if (entity instanceof LivingEntity living && this.affected.add(entity.getUUID())) {
                    living.hurt(player.damageSources().magic(), (float) this.damage);
                }
                entity.setDeltaMovement(dir.scale(this.push));
                entity.hurtMarked = true;
                if (entity.isOnFire()) {
                    entity.clearFire();
                }
            }
        }
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
