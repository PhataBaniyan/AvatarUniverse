package com.phatabaniyan.avataruniverse.bending.ability.fire;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireShield} (shield mode): hold sneak to wrap
 * in a burning sphere that ignites what touches it, swats hostile projectiles
 * out of the sky, and shoves back anything walking through the flames.
 * Reference values: Cooldown 4000ms (80 ticks), Radius 3, FireTicks 3, Push 2.
 */
public class FireShield extends BendingAbility {
    public static final String ID = "FireShield";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIRESHIELD_COOLDOWN_TICKS.get();

    private static final double RADIUS = Config.FIRESHIELD_RADIUS.get();
    private static final int FIRE_SECONDS = Config.FIRESHIELD_FIRE_SECONDS.get();
    private static final double PUSH = Config.FIRESHIELD_PUSH.get();

    private final ServerLevel level;
    private int tick;
    private double ringAngle;

    public FireShield(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
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
        if (player.isEyeInFluid(FluidTags.WATER)) {
            // Source constructor gate: starting underwater fizzles without a
            // cooldown, while dunking mid-hold ends the shield with one.
            if (tick > 0) {
                cool(owner, level, ID, COOLDOWN_TICKS);
            }
            return false;
        }
        if (!player.isShiftKeyDown()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        // No duration end: the shield holds as long as sneak does.
        tick++;
        Vec3 center = new Vec3(player.getX(), player.getEyeY(), player.getZ());

        // Rotating flame shell: latitude bands, staggered phase each tick.
        ringAngle += 0.6;
        int bands = 4;
        for (int band = 0; band <= bands; band++) {
            double polar = Math.PI * band / bands;
            double rr = RADIUS * Math.sin(polar);
            double y = center.y + RADIUS * Math.cos(polar) * 0.8;
            int pts = band == 0 || band == bands ? 1 : 10;
            for (int k = 0; k < pts; k++) {
                double a = ringAngle + k * (Math.PI * 2 / pts);
                level.sendParticles(
                        BendingTheme.particle(Config.FIRESHIELD_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        center.x + Math.cos(a) * rr,
                        y,
                        center.z + Math.sin(a) * rr,
                        Config.FIRESHIELD_FLAME_PARTICLE_COUNT.get(),
                        0.08,
                        0.08,
                        0.08,
                        0.02);
                if (player.getRandom().nextInt(6) == 0) {
                    level.sendParticles(
                            BendingTheme.particle(Config.FIRESHIELD_SHELL_PARTICLE.get(), ParticleTypes.SMOKE),
                            center.x + Math.cos(a) * rr,
                            y,
                            center.z + Math.sin(a) * rr,
                            Config.FIRESHIELD_SHELL_PARTICLE_COUNT.get(),
                            0.08,
                            0.08,
                            0.08,
                            0.02);
                }
            }
        }
        if (player.getRandom().nextInt(12) == 0) {
            level.playSound(
                    null, center.x, center.y, center.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.4F, 1.1F);
        }

        if (tick % 10 == 0) {
            for (Entity e : level.getEntities(player, new AABB(center, center).inflate(RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof Projectile projectile) {
                    projectile.discard();
                    level.sendParticles(
                            BendingTheme.particle(Config.FIRESHIELD_DEFLECT_PARTICLE.get(), owner, ParticleTypes.FLAME),
                            e.getX(),
                            e.getY(),
                            e.getZ(),
                            Config.FIRESHIELD_DEFLECT_PARTICLE_COUNT.get(),
                            0.2,
                            0.2,
                            0.2,
                            0.03);
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.igniteForSeconds(FIRE_SECONDS);
                }
            }
        }
        // Barrier shove every tick: nothing walks through the flames.
        for (Entity e : level.getEntities(player, new AABB(center, center).inflate(RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand || e instanceof Projectile) {
                continue;
            }
            Vec3 to = e.position().subtract(new Vec3(player.getX(), e.getY(), player.getZ()));
            double dist = to.length();
            if (dist > RADIUS) {
                continue;
            }
            Vec3 dir = dist < 1.0e-4 ? new Vec3(0, 1, 0) : to.normalize();
            double falloff = Math.max(0.5, 1.0 - (dist / RADIUS) * 0.5);
            e.setDeltaMovement(dir.x * PUSH * falloff, 0.35 * PUSH * falloff, dir.z * PUSH * falloff);
            e.hurtMarked = true;
            e.resetFallDistance();
        }
        return true;
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
