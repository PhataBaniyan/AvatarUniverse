package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireSki}: click high in the air to drop into a
 * blazing ski-run — fixed fast descent along the gaze that torches whatever is
 * buzzed, until landing, clipping a wall, or the ride timer runs out. Clicking
 * on the ground arms it briefly for the jump-click race; liftoff within the
 * window starts the ride. Reference values: Cooldown 6000ms, Duration 12000ms,
 * Speed 1.2, Ignite true, FireTicks 3, MinHeight 1.
 */
public class FireSki extends BendingAbility {
    public static final String ID = "FireSki";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIRESKI_COOLDOWN_TICKS.get();
    /** Reference Duration 12000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.FIRESKI_DURATION_TICKS.get();
    /** Grounded arm window 600ms, in server ticks. */
    private static final long ARM_TICKS = Config.FIRESKI_ARM_TICKS.get();

    private static final double SPEED = Config.FIRESKI_SPEED.get();
    private static final boolean IGNITE = Config.FIRESKI_IGNITE.get();
    private static final int FIRE_TICKS = Config.FIRESKI_FIRE_SECONDS.get();
    private static final double MIN_HEIGHT = Config.FIRESKI_MIN_HEIGHT.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private boolean started;
    private long rideStart;
    private long armedAt;
    private double ringAngle;
    private boolean dead;

    public FireSki(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        FireSki old = BendingManager.find(player.getUUID(), FireSki.class);
        if (old != null) {
            BendingManager.remove(old);
            this.dead = true;
            return;
        }
        if (badSurface(this.level, player.blockPosition())) {
            this.dead = true;
            return;
        }
        if (!player.onGround() && heightAboveGround(player) >= MIN_HEIGHT) {
            beginRide();
            return;
        }
        // Grounded, or too low to count: arm briefly for the jump-click race.
        this.armedAt = level.getGameTime();
    }

    private void beginRide() {
        this.rideStart = level.getGameTime();
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (dead || !alive(player) || !gate(owner)) {
            return false;
        }
        if (!started) {
            // Armed by a grounded click: ignite on liftoff, fizzle on timeout.
            long now = level.getGameTime();
            if (now - this.armedAt > ARM_TICKS) {
                return false;
            }
            if (player.onGround()) {
                return true;
            }
            if (badSurface(level, player.blockPosition())) {
                return false;
            }
            beginRide();
            return true;
        }
        if (player.onGround() || player.isInWater()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (badSurface(level, player.blockPosition())) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (level.getGameTime() - this.rideStart > DURATION_TICKS) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        Vec3 look = player.getLookAngle().normalize();
        // Feelers ahead: a true wall (face AND knee blocked) ends the ride;
        // a mere bump is climbed over instead of dying to it.
        BlockPos facePos =
                BlockPos.containing(player.getX() + look.x * 0.8, player.getY() + 1.6, player.getZ() + look.z * 0.8);
        BlockPos kneePos =
                BlockPos.containing(player.getX() + look.x * 0.8, player.getY(), player.getZ() + look.z * 0.8);
        boolean faceBlocked =
                level.isLoaded(facePos) && level.getBlockState(facePos).isSolidRender(level, facePos);
        boolean kneeBlocked =
                level.isLoaded(kneePos) && level.getBlockState(kneePos).isSolidRender(level, kneePos);
        if (faceBlocked && kneeBlocked) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        double gap = heightAboveGround(player);
        double vy;
        if (kneeBlocked) {
            vy = 0.7;
        } else if (gap > 1.8) {
            vy = -0.09;
        } else if (gap < 1.7) {
            vy = 0.2;
        } else {
            vy = 0;
        }
        Vec3 v = new Vec3(look.x * SPEED, vy, look.z * SPEED);
        player.setDeltaMovement(v);
        player.hurtMarked = true;
        player.resetFallDistance();
        // Standing ride: real fire disc spinning under the feet.
        this.ringAngle += 0.5;
        for (int k = 0; k < 12; k++) {
            double a = this.ringAngle + k * (Math.PI / 6);
            level.sendParticles(
                    BendingTheme.particle(Config.FIRESKI_RING_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    player.getX() + Math.cos(a) * 0.8,
                    player.getY() + 0.05,
                    player.getZ() + Math.sin(a) * 0.8,
                    Config.FIRESKI_RING_PARTICLE_COUNT.get(),
                    0.08,
                    0.08,
                    0.08,
                    0.02);
        }
        level.sendParticles(
                BendingTheme.particle(Config.FIRESKI_SMOKE_PARTICLE.get(), ParticleTypes.SMOKE),
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                Config.FIRESKI_SMOKE_PARTICLE_COUNT.get(),
                0.3,
                0.3,
                0.3,
                0.03);
        if (IGNITE) {
            Vec3 under = player.position().add(0, -1, 0);
            for (Entity e :
                    level.getEntities(player, new AABB(under, under).inflate(Config.FIRESKI_HIT_RADIUS.get()))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                e.igniteForSeconds(FIRE_TICKS);
                if (e instanceof LivingEntity living) {
                    living.hurt(
                            player.damageSources().magic(),
                            Config.FIRESKI_DAMAGE.get().floatValue());
                }
            }
        }
        return true;
    }

    @Override
    public void onRemove() {
        started = false;
        player.resetFallDistance();
    }

    private static double heightAboveGround(ServerPlayer sp) {
        BlockPos feet = sp.blockPosition();
        ServerLevel level = sp.serverLevel();
        for (int i = 0; i <= 30; i++) {
            BlockPos p = feet.below(i);
            if (!level.isLoaded(p)) {
                return 0;
            }
            if (level.getBlockState(p).isSolidRender(level, p)) {
                return i;
            }
        }
        return 31;
    }

    /** Ice, snow, and open water refuse the ski. */
    private static boolean badSurface(ServerLevel level, BlockPos feet) {
        for (int i = 0; i <= 30; i++) {
            BlockPos p = feet.below(i);
            if (!level.isLoaded(p)) {
                return false;
            }
            var state = level.getBlockState(p);
            if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
                return true;
            }
            if (!state.getFluidState().isEmpty()) {
                return true;
            }
            if (state.is(Blocks.ICE)
                    || state.is(Blocks.PACKED_ICE)
                    || state.is(Blocks.BLUE_ICE)
                    || state.is(Blocks.FROSTED_ICE)) {
                return true;
            }
            if (state.isSolidRender(level, p)) {
                return false;
            }
        }
        return false;
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
