package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirSuction}: sneak-hold vacuum that drags
 * everything in front of the caster (mobs, items, players) along the gaze,
 * stripping flames off on arrival. Releasing sneak ends it and starts the
 * cooldown. Reference values: Cooldown 2000ms (40 ticks), Range 20,
 * Radius 3 (close-range grab), Push 1.2, 35-degree cone.
 */
public class AirSuction extends BendingAbility {
    public static final String ID = "AirSuction";

    /** Reference Cooldown 2000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.AIRSUCTION_COOLDOWN_TICKS.get();

    private static final double RANGE = Config.AIRSUCTION_RANGE.get();
    private static final double RADIUS = Config.AIRSUCTION_RADIUS.get();
    private static final double PUSH = Config.AIRSUCTION_PUSH.get();
    private static final double CONE_COS = Math.cos(Math.toRadians(Config.AIRSUCTION_CONE_DEGREES.get()));

    private final ServerLevel level;
    private double ringAngle = 0;
    private boolean invalid = false;

    public AirSuction(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        if (player.isEyeInFluid(FluidTags.WATER)) {
            this.invalid = true;
        }
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
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(owner);
        if (sp == null || sp.hasDisconnected() || !sp.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (!sp.isShiftKeyDown()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        Vec3 center = sp.position().add(0, 1.2, 0);
        Vec3 look = sp.getLookAngle().normalize();
        this.ringAngle += 0.7;

        // Inward spiral around the caster: the visible throat of the vacuum.
        for (int k = 0; k < 6; k++) {
            double a = this.ringAngle + k * (Math.PI / 3);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSUCTION_MAIN_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    sp.getX() + Math.cos(a) * 1.5,
                    sp.getY() + 1.2,
                    sp.getZ() + Math.sin(a) * 1.5,
                    Config.AIRSUCTION_MAIN_PARTICLE_COUNT.get(),
                    0.1,
                    0.1,
                    0.1,
                    0.05);
        }
        if (sp.getRandom().nextInt(10) == 0) {
            level.playSound(
                    null,
                    sp.getX(),
                    sp.getY(),
                    sp.getZ(),
                    SoundEvents.WIND_CHARGE_BURST,
                    SoundSource.PLAYERS,
                    0.35F,
                    0.7F);
        }

        for (Entity e : level.getEntities(sp, new AABB(center, center).inflate(RANGE))) {
            if (e.getUUID().equals(sp.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            Vec3 toEntity = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(center);
            double dist = toEntity.length();
            if (dist > RANGE || dist < 1.0e-4) {
                continue;
            }
            Vec3 dir = toEntity.normalize();
            // Must be roughly in front, or close enough to be grabbed anyway.
            if (dir.dot(look) < CONE_COS && dist > RADIUS) {
                continue;
            }
            double strength = PUSH * Math.max(0.4, 1.0 - (dist / RANGE) * 0.5);
            Vec3 pull = dir.scale(-strength);
            e.setDeltaMovement(pull.x, pull.y + 0.1 * strength, pull.z);
            e.hurtMarked = true;
            e.resetFallDistance();
            if (e.isOnFire()) {
                e.clearFire();
            }
            // Stream lines from victim to caster so the drag reads at distance.
            Vec3 mid = center.add(toEntity.scale(0.5));
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSUCTION_STREAM_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    mid.x,
                    mid.y,
                    mid.z,
                    Config.AIRSUCTION_STREAM_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.08);
        }
        return true;
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
