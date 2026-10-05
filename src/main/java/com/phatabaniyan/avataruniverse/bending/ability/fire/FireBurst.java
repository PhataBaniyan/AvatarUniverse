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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireBurst}: hold sneak to charge, release for
 * an instant radial detonation, or left-click while charged to fan the blast
 * in a cone down the gaze. Reference values: Cooldown 3000ms (60 ticks),
 * Charge 1500ms (30 ticks), Damage 3, Radius 12, Push 2.
 */
public class FireBurst extends BendingAbility {
    public static final String ID = "FireBurst";

    private static final double CONE_ANGLE = Math.toRadians(30);
    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIREBURST_COOLDOWN_MS.get());
    /** Reference charge 1500ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.FIREBURST_CHARGE_MS.get());

    private static final double DAMAGE = Config.FIREBURST_DAMAGE.get();
    private static final double RADIUS = Config.FIREBURST_RADIUS.get();
    private static final double PUSH = Config.FIREBURST_PUSH.get();

    private final ServerLevel level;
    private boolean charged;

    public FireBurst(ServerPlayer player) {
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
            if (charged) {
                sphereBurst(player);
            }
            return false;
        }
        if (!charged && level.getGameTime() - startTime >= CHARGE_TICKS) {
            charged = true;
        }
        if (charged) {
            Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
            level.sendParticles(
                    BendingTheme.particle(Config.FIREBURST_CHARGE_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    eye.x,
                    eye.y,
                    eye.z,
                    Config.FIREBURST_CHARGE_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.02);
        }
        return true;
    }

    private void sphereBurst(ServerPlayer player) {
        Vec3 origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        for (Entity e : level.getEntities(player, new AABB(origin, origin).inflate(RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(origin);
            double dist = to.length();
            if (dist > RADIUS) {
                continue;
            }
            Vec3 dir = dist < 1.0e-4 ? new Vec3(0, 1, 0) : to.normalize();
            double falloff = Math.max(0.3, 1.0 - (dist / RADIUS) * 0.5);
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), (float) (DAMAGE * falloff));
                living.igniteForSeconds(Config.FIREBURST_FIRE_MS.get() / 1000);
            }
            Vec3 v = dir.scale(PUSH * falloff);
            e.setDeltaMovement(v.x, v.y + 0.45 * PUSH * falloff, v.z);
            e.hurtMarked = true;
            e.resetFallDistance();
        }
        for (double f : new double[] {0.3, 0.5, 0.7, 0.85, 1.0}) {
            int pts = 16;
            for (int k = 0; k < pts; k++) {
                double a = k * (Math.PI * 2 / pts);
                level.sendParticles(
                        BendingTheme.particle(Config.FIREBURST_BURST_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        origin.x + Math.cos(a) * RADIUS * f,
                        origin.y,
                        origin.z + Math.sin(a) * RADIUS * f,
                        Config.FIREBURST_BURST_PARTICLE_COUNT.get(),
                        0.15,
                        0.15,
                        0.15,
                        0.03);
                level.sendParticles(
                        BendingTheme.particle(Config.FIREBURST_RING_PARTICLE.get(), ParticleTypes.SMOKE),
                        origin.x + Math.cos(a) * RADIUS * f,
                        origin.y + 0.5,
                        origin.z + Math.sin(a) * RADIUS * f,
                        Config.FIREBURST_RING_PARTICLE_COUNT.get(),
                        0.15,
                        0.15,
                        0.15,
                        0.02);
            }
        }
        int bands = 6;
        for (int band = 1; band < bands; band++) {
            double polar = Math.PI * band / bands;
            double rr = RADIUS * Math.sin(polar);
            double y = origin.y + RADIUS * Math.cos(polar);
            for (int k = 0; k < 16; k++) {
                double a = k * (Math.PI / 8);
                level.sendParticles(
                        BendingTheme.particle(Config.FIREBURST_SPHERE_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        origin.x + Math.cos(a) * rr,
                        y,
                        origin.z + Math.sin(a) * rr,
                        Config.FIREBURST_SPHERE_PARTICLE_COUNT.get(),
                        0.12,
                        0.12,
                        0.12,
                        0.03);
            }
        }
        level.playSound(
                null, origin.x, origin.y, origin.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6F, 1.2F);
        scorchGround(level, origin, RADIUS, 40, 140);
        cool(owner, level, ID, COOLDOWN_TICKS);
    }

    /** Set the ground alight where the sphere touches down. Shared with FireSpin. */
    public static void scorchGround(ServerLevel level, Vec3 center, double radius) {
        scorchGround(level, center, radius, 24, 80);
    }

    public static void scorchGround(ServerLevel level, Vec3 center, double radius, int max, int tryCount) {
        int lit = 0;
        int n = 0;
        var random = level.random;
        while (lit < max && n < tryCount) {
            n++;
            double a = random.nextDouble() * Math.PI * 2;
            double d = radius * Math.sqrt(random.nextDouble());
            double px = center.x + Math.cos(a) * d;
            double pz = center.z + Math.sin(a) * d;
            for (int dy = 3; dy >= -5; dy--) {
                BlockPos p = BlockPos.containing(px, center.y + dy, pz);
                if (!level.isLoaded(p) || !level.isLoaded(p.below())) {
                    break;
                }
                if (level.getBlockState(p).isAir()
                        && level.getBlockState(p.below()).isSolidRender(level, p.below())) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                    lit++;
                    break;
                }
                if (level.getBlockState(p).isSolidRender(level, p)) {
                    break;
                }
            }
        }
    }

    /** Left-click while a charged instance is live. */
    public static void tryConeBurst(ServerPlayer player) {
        FireBurst burst = BendingManager.find(player.getUUID(), FireBurst.class);
        if (burst == null || !burst.isCharged()) {
            return;
        }
        burst.coneBurst(player);
        BendingManager.remove(burst);
    }

    private void coneBurst(ServerPlayer player) {
        Vec3 origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        double range = RADIUS * 1.3;
        for (Entity e : level.getEntities(player, new AABB(origin, origin).inflate(range))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
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
            double falloff = Math.max(0.3, 1.0 - (dist / range) * 0.5);
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), (float) (DAMAGE * falloff));
                living.igniteForSeconds(Config.FIREBURST_FIRE_MS.get() / 1000);
            }
            Vec3 v = dir.scale(PUSH * 1.2 * falloff);
            e.setDeltaMovement(v.x, v.y + 0.4 * PUSH * falloff, v.z);
            e.hurtMarked = true;
            e.resetFallDistance();
        }
        // Dense flame cone down the gaze.
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        Vec3 up = side.cross(look).normalize();
        for (int step = 1; step <= 6; step++) {
            double d = step * range / 6;
            double r = d * Math.tan(CONE_ANGLE);
            Vec3 c = origin.add(look.scale(d));
            for (int k = 0; k < 10; k++) {
                double a = k * (Math.PI / 5);
                Vec3 p = c.add(side.scale(Math.cos(a) * r)).add(up.scale(Math.sin(a) * r));
                level.sendParticles(
                        BendingTheme.particle(Config.FIREBURST_CONE_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        p.x,
                        p.y,
                        p.z,
                        Config.FIREBURST_CONE_PARTICLE_COUNT.get(),
                        0.15,
                        0.15,
                        0.15,
                        0.03);
            }
            level.sendParticles(
                    BendingTheme.particle(Config.FIREBURST_CORE_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    c.x,
                    c.y,
                    c.z,
                    Config.FIREBURST_CORE_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.04);
        }
        level.playSound(
                null, origin.x, origin.y, origin.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 0.9F);
        cool(owner, level, ID, COOLDOWN_TICKS);
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
