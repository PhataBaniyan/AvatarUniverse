package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
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
 * Port of ProjectAvatar {@code FireComet}: hold sneak to gather the comet at
 * a point ahead, release (after the charge completes) to send it screaming
 * down the gaze. Releasing before the charge finishes fizzles with no
 * cooldown; the launched comet keeps flying after sneak is released. Impact
 * hurts living entities, explodes, and scorches the ground. Reference values:
 * Cooldown 7000ms (140 ticks), Charge 2500ms (50 ticks), Damage 7,
 * BlastRadius 4, Range 25.
 */
public class FireComet extends BendingAbility {
    public static final String ID = "FireComet";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIRECOMET_COOLDOWN_MS.get());
    /** Reference Charge 2500ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.FIRECOMET_CHARGE_MS.get());
    /** Scorch-fire revert, in server ticks (WallOfFire footing parity). */
    private static final long SCORCH_REVERT_TICKS = Config.msToTicks(Config.FIRECOMET_SCORCH_REVERT_MS.get());

    private static final float DAMAGE = Config.FIRECOMET_DAMAGE.get().floatValue();
    private static final double BLAST_RADIUS = Config.FIRECOMET_BLAST_RADIUS.get();
    private static final double RANGE = Config.FIRECOMET_RANGE.get();
    private static final int BLAST_FIRE_SECONDS = Config.FIRECOMET_BLAST_FIRE_MS.get() / 1000;

    private final ServerPlayer player;
    private final ServerLevel level;
    private boolean fired = false;
    private Vec3 pos;
    private Vec3 dir;
    private Vec3 launchLoc;
    private double travelled = 0;
    private double ringAngle = 0;
    private int point = 0;
    private boolean chargeSoundPlayed = false;

    public FireComet(ServerPlayer player) {
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
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!this.fired) {
            if (player.isShiftKeyDown()) {
                Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
                Vec3 look = player.getLookAngle().normalize();
                this.pos = eye.add(look.scale(6));
                chargeRings(this.pos, player.getYRot());
                return true;
            }
            if (level.getGameTime() - this.startTime < CHARGE_TICKS) {
                return false;
            }
            this.dir = player.getLookAngle().normalize();
            this.launchLoc = this.pos;
            this.travelled = 0;
            this.fired = true;
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F);
            return true;
        }
        this.travelled += 1.0;
        if (this.travelled >= RANGE) {
            return false;
        }
        this.pos = this.pos.add(this.dir);
        BlockPos bp = BlockPos.containing(this.pos);
        if (!level.isLoaded(bp)) {
            return false;
        }
        var state = level.getBlockState(bp);
        if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
            blast();
            return false;
        }
        for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(3.0))) {
            if (e instanceof LivingEntity && !e.getUUID().equals(player.getUUID()) && !(e instanceof ArmorStand)) {
                blast();
                return false;
            }
        }
        cometRings(this.pos, player.getYRot());
        if (player.getRandom().nextInt(2) == 0) {
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.PLAYERS,
                    0.4F,
                    1.2F);
        }
        return true;
    }

    private void tripleRings(Vec3 at, float yaw, double size, double spin, int flameCount) {
        double yawRad = Math.toRadians(yaw);
        double[] tilts = {Math.PI / 3 * 2.1 / 2, -Math.PI / 3 * 2.1 / 2, Math.PI / 2};
        for (double tilt : tilts) {
            for (int k = 0; k < 8; k++) {
                double a = spin + k * (Math.PI / 4);
                Vec3 v = rotX(new Vec3(Math.cos(a) * size, Math.sin(a) * size, 0), tilt);
                v = rotY(v, -(yawRad - 1.575));
                level.sendParticles(
                        BendingTheme.particle(Config.FIRECOMET_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        at.x + v.x,
                        at.y + v.y,
                        at.z + v.z,
                        flameCount,
                        0.06,
                        0.06,
                        0.06,
                        0.02);
                if (flameCount > 1) {
                    level.sendParticles(
                            BendingTheme.particle(Config.FIRECOMET_RING_PARTICLE.get(), ParticleTypes.SMOKE),
                            at.x + v.x,
                            at.y + v.y,
                            at.z + v.z,
                            Config.FIRECOMET_RING_PARTICLE_COUNT.get(),
                            0.08,
                            0.08,
                            0.08,
                            0.03);
                }
            }
        }
    }

    private void chargeRings(Vec3 at, float yaw) {
        this.ringAngle += 0.4;
        if (!this.chargeSoundPlayed && level.getGameTime() - this.startTime >= CHARGE_TICKS) {
            this.chargeSoundPlayed = true;
            level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.4F);
        }
        tripleRings(at, yaw, 2.2, this.ringAngle, 1);
    }

    private void cometRings(Vec3 at, float yaw) {
        this.point++;
        if (this.point >= 360) {
            this.point = 0;
        }
        tripleRings(at, yaw, 2.2, this.point, 2);
    }

    private static Vec3 rotX(Vec3 v, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return new Vec3(v.x, v.y * cos - v.z * sin, v.y * sin + v.z * cos);
    }

    private static Vec3 rotY(Vec3 v, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return new Vec3(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
    }

    private void blast() {
        Vec3 at = this.pos;
        for (Entity e : level.getEntities(player, new AABB(at, at).inflate(BLAST_RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), DAMAGE);
                living.igniteForSeconds(BLAST_FIRE_SECONDS);
            }
        }
        level.explode(null, at.x, at.y, at.z, 2.5F, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        scorchGround(at, BLAST_RADIUS);
        level.sendParticles(
                BendingTheme.particle(Config.FIRECOMET_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                at.x,
                at.y,
                at.z,
                Config.FIRECOMET_BLAST_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.1);
        level.sendParticles(
                BendingTheme.particle(Config.FIRECOMET_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                at.x,
                at.y,
                at.z,
                Config.FIRECOMET_FLAME_PARTICLE_COUNT.get(),
                0.7,
                0.7,
                0.7,
                0.08);
        level.sendParticles(
                BendingTheme.particle(Config.FIRECOMET_SPARK_PARTICLE.get(), ParticleTypes.FIREWORK),
                at.x,
                at.y,
                at.z,
                Config.FIRECOMET_SPARK_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.08);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.9F, 0.9F);
        cool(owner, level, ID, COOLDOWN_TICKS);
    }

    /** Set the ground alight where the comet touches down (FireBurst parity, reverting). */
    private void scorchGround(Vec3 center, double radius) {
        long now = level.getGameTime();
        int lit = 0;
        int n = 0;
        var random = level.random;
        while (lit < 24 && n < 80) {
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
                    TempBlock temp = new TempBlock(level, p, Blocks.FIRE.defaultBlockState(), TempBlock.QUIET);
                    BendingManager.scheduleRevert(temp, now + SCORCH_REVERT_TICKS);
                    lit++;
                    break;
                }
                if (level.getBlockState(p).isSolidRender(level, p)) {
                    break;
                }
            }
        }
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
