package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
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
 * Port of ProjectAvatar {@code LightningBurst} (JedCore port).
 * Hold sneak to charge, release to fire a full sphere of jittering bolts.
 * Angles match the reference exactly: yaw -180..180 step 55 x pitch
 * -180..180 step 55 (~49 bolts), each stepping 0.2 with gap 1 and +-20 deg
 * jitter. Reference values: Cooldown 6000ms (120 ticks), Charge 2000ms
 * (40 ticks), Damage 5, Radius 20.
 */
public class LightningBurst extends BendingAbility {
    public static final String ID = "LightningBurst";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.LIGHTNINGBURST_COOLDOWN_MS.get());
    /** Reference charge 2000ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.LIGHTNINGBURST_CHARGE_MS.get());

    private static final double DAMAGE = Config.LIGHTNINGBURST_DAMAGE.get();
    private static final double RADIUS = Config.LIGHTNINGBURST_RADIUS.get();
    private static final int YAW_STEP = Config.LIGHTNINGBURST_YAW_STEP.get();
    private static final int PITCH_STEP = Config.LIGHTNINGBURST_PITCH_STEP.get();
    private static final double STEP_LENGTH = Config.LIGHTNINGBURST_STEP_LENGTH.get();
    private static final double GAP_LENGTH = Config.LIGHTNINGBURST_GAP_LENGTH.get();
    private static final int JITTER_DEGREES = Config.LIGHTNINGBURST_JITTER_DEGREES.get();
    private static final double HIT_RADIUS = Config.LIGHTNINGBURST_HIT_RADIUS.get();
    private static final int IGNITE_SECONDS = Config.LIGHTNINGBURST_IGNITE_MS.get() / 1000;

    private record Bolt(Vec3 pos, Vec3 dir, double dist, float yaw, float pitch) {}

    private final ServerPlayer player;
    private final ServerLevel level;
    private boolean charged = false;
    private boolean fired = false;
    private final List<Bolt> bolts = new ArrayList<>();
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public LightningBurst(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || player.hasDisconnected() || !gate(owner)) {
            return false;
        }
        if (!this.fired) {
            if (player.isShiftKeyDown()) {
                if (!this.charged && level.getGameTime() - this.startTime >= CHARGE_TICKS) {
                    this.charged = true;
                }
                if (this.charged) {
                    displayCharging();
                }
                return true;
            }
            if (!this.charged) {
                return false;
            }
            // Release: full sphere, reference yaw/pitch lattice.
            Vec3 center = player.position().add(0, 1, 0);
            for (int yawDeg = -180; yawDeg < 180; yawDeg += YAW_STEP) {
                for (int pitchDeg = -180; pitchDeg <= 180; pitchDeg += PITCH_STEP) {
                    double yawRad = Math.toRadians(yawDeg);
                    double pitchRad = Math.toRadians(pitchDeg);
                    // Fake point on a shell, then aim from the player at it.
                    Vec3 shell = new Vec3(
                            center.x - Math.sin(yawRad) * Math.cos(pitchRad) * 2,
                            center.y + Math.sin(pitchRad) * 2,
                            center.z + Math.cos(yawRad) * Math.cos(pitchRad) * 2);
                    Vec3 dir = shell.subtract(center);
                    if (dir.lengthSqr() < 1.0e-6) {
                        continue;
                    }
                    dir = dir.normalize();
                    this.bolts.add(new Bolt(center, dir, 0, (float) yawDeg, (float) pitchDeg));
                }
            }
            this.fired = true;
            cool(owner, level, ID, COOLDOWN_TICKS);
            level.playSound(
                    null,
                    center.x,
                    center.y,
                    center.z,
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    1.0F,
                    1.2F);
            return true;
        }
        if (this.bolts.isEmpty()) {
            return false;
        }
        for (int idx = 0; idx < this.bolts.size(); idx++) {
            Bolt b = this.bolts.get(idx);
            Vec3 loc = b.pos();
            float yaw = b.yaw();
            float pitch = b.pitch();
            double dist = b.dist();
            boolean dead = false;
            // Gap 1.0 in 0.2 steps, like the reference Bolt.
            for (double g = 0; g < GAP_LENGTH; g += STEP_LENGTH) {
                dist += STEP_LENGTH;
                Vec3 dir = b.dir();
                loc = loc.add(dir.scale(STEP_LENGTH));
                level.sendParticles(
                        BendingTheme.particle(Config.LIGHTNINGBURST_TRAIL_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                        loc.x,
                        loc.y,
                        loc.z,
                        Config.LIGHTNINGBURST_TRAIL_PARTICLE_COUNT.get(),
                        0.0,
                        0.0,
                        0.0,
                        0.0);
                BlockPos bp = BlockPos.containing(loc);
                if (!level.isLoaded(bp) || dist > RADIUS) {
                    dead = true;
                    break;
                }
                var state = level.getBlockState(bp);
                if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                    dead = true;
                    break;
                }
            }
            if (dead) {
                this.bolts.remove(idx--);
                continue;
            }
            // Jitter +-20 deg like the reference arc parameter.
            int pick = ThreadLocalRandom.current().nextInt(3);
            if (pick == 0) {
                yaw -= JITTER_DEGREES;
            } else if (pick == 1) {
                yaw += JITTER_DEGREES;
            }
            pick = ThreadLocalRandom.current().nextInt(3);
            if (pick == 0) {
                pitch -= JITTER_DEGREES;
            } else if (pick == 1) {
                pitch += JITTER_DEGREES;
            }
            double yr = Math.toRadians(yaw);
            double pr = Math.toRadians(pitch);
            Vec3 ndir = new Vec3(-Math.sin(yr) * Math.cos(pr), Math.sin(pr), Math.cos(yr) * Math.cos(pr));
            if (ndir.lengthSqr() < 1.0e-6) {
                ndir = b.dir();
            } else {
                ndir = ndir.normalize();
            }
            if (ThreadLocalRandom.current().nextInt(3) == 0) {
                level.playSound(null, loc.x, loc.y, loc.z, SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 0.3F, 1.0F);
            }
            boolean consumed = false;
            for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(HIT_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                    living.hurt(player.damageSources().lightningBolt(), (float) DAMAGE);
                    living.igniteForSeconds(IGNITE_SECONDS);
                }
                consumed = true;
                break;
            }
            if (consumed) {
                this.bolts.remove(idx--);
                continue;
            }
            this.bolts.set(idx, new Bolt(loc, ndir, dist, yaw, pitch));
        }
        return !this.bolts.isEmpty();
    }

    /** Incoming sparks while charged, like the reference displayCharging. */
    private void displayCharging() {
        Vec3 center = player.position().add(0, 1, 0);
        for (int i = 0; i < 6; i++) {
            double yaw = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
            double pitch = (ThreadLocalRandom.current().nextDouble() - 0.5) * Math.PI;
            Vec3 shell = new Vec3(
                    center.x - Math.sin(yaw) * Math.cos(pitch) * 1.2,
                    center.y + Math.sin(pitch) * 1.2 + 1.0,
                    center.z + Math.cos(yaw) * Math.cos(pitch) * 1.2);
            level.sendParticles(
                    BendingTheme.particle(Config.LIGHTNINGBURST_CHARGE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    shell.x,
                    shell.y,
                    shell.z,
                    Config.LIGHTNINGBURST_CHARGE_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.0);
        }
        level.sendParticles(
                BendingTheme.particle(Config.LIGHTNINGBURST_CORE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                center.x,
                center.y,
                center.z,
                Config.LIGHTNINGBURST_CORE_PARTICLE_COUNT.get(),
                0.6,
                0.6,
                0.6,
                0.05);
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
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
