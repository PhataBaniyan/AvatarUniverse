package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirBurst}: hold sneak to charge, release for
 * a radial sphere burst, or left-click while charged for a 30-degree cone
 * burst. Hard landings with AirBurst bound erupt on their own (fall burst).
 * Reference values: Cooldown 2500ms (50 ticks), Charge 1500ms (30 ticks),
 * Damage 2, Radius 7, Push 2.2, FallThreshold 8.
 */
public class AirBurst extends BendingAbility {
    public static final String ID = "AirBurst";

    private static final double CONE_ANGLE_DEGREES = Config.AIRBURST_CONE_ANGLE_DEGREES.get();
    private static final double CONE_ANGLE = Math.toRadians(CONE_ANGLE_DEGREES);
    /** Reference Cooldown 2500ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.msToTicks(Config.AIRBURST_COOLDOWN_MS.get());
    /** Reference charge 1500ms, in server ticks. */
    private static final int CHARGE_TICKS = Config.msToTicks(Config.AIRBURST_CHARGE_MS.get());

    private static final double DAMAGE = Config.AIRBURST_DAMAGE.get();
    private static final double RADIUS = Config.AIRBURST_RADIUS.get();
    private static final double PUSH = Config.AIRBURST_PUSH.get();
    private static final double FALL_THRESHOLD = Config.AIRBURST_FALL_THRESHOLD.get();
    private static final double CONE_RANGE_MULT = Config.AIRBURST_CONE_RANGE_MULT.get();
    private static final double CONE_PUSH_MULT = Config.AIRBURST_CONE_PUSH_MULT.get();
    private static final double LIFT = Config.AIRBURST_LIFT.get();

    private final ServerLevel level;
    private boolean charged = false;
    private double ringAngle = 0;

    public AirBurst(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    public boolean isCharged() {
        return charged;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            if (this.charged) {
                sphereBurst(player);
            }
            return false;
        }
        if (!this.charged && level.getGameTime() - this.startTime >= CHARGE_TICKS) {
            this.charged = true;
        }
        if (this.charged) {
            chargeParticles(player);
        }
        return true;
    }

    private void chargeParticles(ServerPlayer player) {
        Vec3 center = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        this.ringAngle += 0.6;
        for (int k = 0; k < 6; k++) {
            double a = this.ringAngle + k * (Math.PI / 3);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBURST_CHARGE_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    center.x + Math.cos(a) * 0.7,
                    center.y,
                    center.z + Math.sin(a) * 0.7,
                    Config.AIRBURST_CHARGE_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
        }
    }

    private static void affect(
            ServerPlayer player, Entity e, Vec3 dir, double dist, double radius, double push, double damage) {
        if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
            return;
        }
        // Damage first: hurt() halves existing velocity, so the push must land after it.
        if (e instanceof LivingEntity living) {
            living.hurt(player.damageSources().magic(), (float) damage);
        }
        double falloff = Math.max(0.3, 1.0 - (dist / radius) * 0.5);
        Vec3 v = dir.scale(push * falloff);
        e.setDeltaMovement(v.x, v.y + LIFT * push * falloff, v.z);
        e.hurtMarked = true;
        e.resetFallDistance();
    }

    private void sphereBurst(ServerPlayer player) {
        Vec3 origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        for (Entity e : level.getEntities(player, new AABB(origin, origin).inflate(RADIUS))) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(origin);
            double dist = to.length();
            if (dist > RADIUS) {
                continue;
            }
            Vec3 dir = dist < 1.0e-4 ? new Vec3(0, 1, 0) : to.normalize();
            affect(player, e, dir, dist, RADIUS, PUSH, DAMAGE);
        }
        for (double f : new double[] {0.35, 0.65, 1.0}) {
            ring(level, origin, RADIUS * f, (int) (RADIUS * 2));
        }
        sphereShell(level, origin, RADIUS);
        level.playSound(
                null, origin.x, origin.y, origin.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.8F, 0.9F);
        cool(owner, level, ID, COOLDOWN_TICKS);
    }

    /** Left-click while a charged instance is live. */
    public static void tryConeBurst(ServerPlayer player) {
        AirBurst burst = BendingManager.find(player.getUUID(), AirBurst.class);
        if (burst == null || !burst.isCharged()) {
            return;
        }
        burst.coneBurst(player);
        BendingManager.remove(burst);
    }

    private void coneBurst(ServerPlayer player) {
        Vec3 origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        double range = RADIUS * CONE_RANGE_MULT;
        for (Entity e : level.getEntities(player, new AABB(origin, origin).inflate(range))) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(origin);
            double dist = to.length();
            if (dist > range || dist < 1.0e-4) {
                continue;
            }
            Vec3 dir = to.normalize();
            double angle = Math.acos(Math.min(1.0, Math.max(-1.0, dir.dot(look))));
            if (angle > CONE_ANGLE) {
                continue;
            }
            affect(player, e, dir, dist, range, PUSH * CONE_PUSH_MULT, DAMAGE);
        }
        for (int step = 1; step <= 3; step++) {
            Vec3 c = origin.add(look.scale(step * range / 3));
            ring(level, c, step * range / 6, (int) RADIUS);
        }
        level.playSound(
                null, origin.x, origin.y, origin.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.8F, 1.1F);
        cool(owner, level, ID, COOLDOWN_TICKS);
    }

    private static void ring(ServerLevel level, Vec3 center, double r, int count) {
        for (int k = 0; k < 12; k++) {
            double a = k * (Math.PI / 6);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBURST_RING_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    center.x + Math.cos(a) * r,
                    center.y,
                    center.z + Math.sin(a) * r,
                    Math.max(1, Config.AIRBURST_RING_PARTICLE_COUNT.get() * count / 12),
                    0.15,
                    0.15,
                    0.15,
                    0.02);
        }
    }

    /** Full 360-degree sphere shell so the burst reads from every side, top included. */
    private static void sphereShell(ServerLevel level, Vec3 center, double r) {
        int bands = 5;
        for (int band = 0; band <= bands; band++) {
            double polar = Math.PI * band / bands;
            double rr = r * Math.sin(polar);
            double y = center.y + r * Math.cos(polar);
            int pts = band == 0 || band == bands ? 1 : 12;
            for (int k = 0; k < pts; k++) {
                double a = k * (Math.PI * 2 / pts);
                level.sendParticles(
                        BendingTheme.particle(Config.AIRBURST_SHELL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        center.x + Math.cos(a) * rr,
                        y,
                        center.z + Math.sin(a) * rr,
                        Config.AIRBURST_SHELL_PARTICLE_COUNT.get(),
                        0.12,
                        0.12,
                        0.12,
                        0.02);
            }
        }
    }

    /** Hard landing with AirBurst bound: horizontal shockwave. */
    public static void tryFallBurst(ServerPlayer sp, float distance) {
        BendingPlayer bending = BendingPlayer.get(sp.getUUID());
        if (bending == null || !bending.hasElement(BendingElement.AIR)) {
            return;
        }
        String bound = bending.boundAbility(sp.getInventory().selected + 1);
        if (!ID.equalsIgnoreCase(bound)
                || bending.isOnCooldown(ID, sp.serverLevel().getGameTime())) {
            return;
        }
        if (distance < FALL_THRESHOLD) {
            return;
        }
        ServerLevel level = sp.serverLevel();
        Vec3 origin = sp.position();
        for (Entity e : level.getEntities(sp, new AABB(origin, origin).inflate(RADIUS, 3, RADIUS))) {
            Vec3 to = e.position().subtract(origin);
            to = new Vec3(to.x, 0, to.z);
            double dist = to.length();
            if (dist > RADIUS) {
                continue;
            }
            Vec3 dir = dist < 1.0e-4 ? new Vec3(0, 1, 0) : to.normalize();
            affect(sp, e, dir, dist, RADIUS, PUSH, DAMAGE);
        }
        ring(level, new Vec3(origin.x, origin.y + 0.3, origin.z), RADIUS, (int) (RADIUS * 3));
        level.playSound(
                null, origin.x, origin.y, origin.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.9F, 0.7F);
        sp.resetFallDistance();
        cool(sp.getUUID(), level, ID, COOLDOWN_TICKS);
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
