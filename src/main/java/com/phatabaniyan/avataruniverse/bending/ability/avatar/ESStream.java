package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of ProjectAvatar {@code avatar.sphere.ESStream}: four-element finisher
 * fired from hotbar slot 5 while ElementSphere is active. Requires one use
 * of every element left, ends the sphere on cast, and bursts into a
 * reverting crater on impact. Reference values: Cooldown 8000ms (160 ticks),
 * Range 30, Damage 6, Knockback 2, RequiredUses 1, EndAbility true, Crater 3,
 * Revert 8000ms.
 */
public class ESStream extends SphereAttack {
    public static final String ID = "ESStream";

    /** Reference Cooldown 8000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.ESSTREAM_COOLDOWN_TICKS.get();

    private static final double RANGE = Config.ESSTREAM_RANGE.get();
    private static final double DAMAGE = Config.ESSTREAM_DAMAGE.get();
    private static final double KNOCKBACK = Config.ESSTREAM_KNOCKBACK.get();
    private static final int REQUIRED_USES = Config.ESSTREAM_REQUIRED_USES.get();
    private static final boolean END_ABILITY = Config.ESSTREAM_END_ABILITY.get();
    private static final int CRATER = Config.ESSTREAM_CRATER.get();
    private static final long REVERT_MS = Config.ESSTREAM_REVERT_MS.get();
    private static final double SPEED = Config.ESSTREAM_SPEED.get();

    private Vec3 pos;
    private Vec3 origin;
    private Vec3 dir;
    private int angle = 0;
    private final List<FallingBlockEntity> debris = new ArrayList<>();
    private int debrisAge = 0;

    public ESStream(ServerPlayer player) {
        super(player);
        if (!gate(player.getUUID())) {
            return;
        }
        ElementSphere sphere = BendingManager.find(player.getUUID(), ElementSphere.class);
        if (sphere == null) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || bending.isOnCooldown(ID, level.getGameTime())) {
            return;
        }
        if (sphere.getAirUses() < REQUIRED_USES
                || sphere.getEarthUses() < REQUIRED_USES
                || sphere.getFireUses() < REQUIRED_USES
                || sphere.getWaterUses() < REQUIRED_USES) {
            return;
        }
        if (END_ABILITY) {
            BendingManager.remove(sphere);
        } else {
            sphere.setAirUses(sphere.getAirUses() - REQUIRED_USES);
            sphere.setEarthUses(sphere.getEarthUses() - REQUIRED_USES);
            sphere.setFireUses(sphere.getFireUses() - REQUIRED_USES);
            sphere.setWaterUses(sphere.getWaterUses() - REQUIRED_USES);
        }
        bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);

        Vec3 eye = eyePos(player);
        this.origin = eye;
        this.pos = eye;
        this.dir = player.getLookAngle().normalize();
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive()) {
            clearDebris();
            return false;
        }
        ServerPlayer sp = player;
        if (this.origin.distanceTo(this.pos) >= RANGE) {
            clearDebris();
            return false;
        }

        for (Entity entity : level.getEntities(sp, new AABB(pos, pos).inflate(1.5))) {
            if (entity.getUUID().equals(sp.getUUID())
                    || !(entity instanceof LivingEntity living)
                    || entity instanceof ArmorStand) {
                continue;
            }
            living.setDeltaMovement(this.dir.scale(KNOCKBACK));
            living.hurtMarked = true;
            living.hurt(sp.damageSources().magic(), (float) DAMAGE);
        }

        if (sp.isAlive()) {
            this.dir = sp.getLookAngle().normalize();
        }
        this.pos = this.pos.add(this.dir.scale(SPEED));

        BlockPos bp = BlockPos.containing(this.pos);
        if (!level.getBlockState(bp).isAir()) {
            impact(sp);
            return false;
        }

        this.angle += 20;
        if (this.angle > 360) {
            this.angle = 0;
        }
        DustParticleOptions water = new DustParticleOptions(new Vector3f(0.02F, 0.76F, 1.0F), 1.0F);
        DustParticleOptions earth = new DustParticleOptions(new Vector3f(0.46F, 0.28F, 0.10F), 1.0F);
        for (int i = 0; i < 4; i++) {
            for (double d = -4; d <= 0; d += 0.5) {
                if (this.origin.distanceTo(this.pos) < -d) {
                    continue;
                }
                Vec3 l = this.pos.add(this.dir.scale(d));
                double r = Math.min(0.75, -d / 5);
                Vec3 ov = orthogonal(this.dir, this.angle + 90 * i + d, r);
                Vec3 pl = l.add(ov);
                switch (i) {
                    case 0 -> level.sendParticles(
                            BendingTheme.particle(Config.ESSTREAM_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                            pl.x,
                            pl.y,
                            pl.z,
                            Config.ESSTREAM_TRAIL_PARTICLE_COUNT.get(),
                            0.05,
                            0.05,
                            0.05,
                            0.005);
                    case 1 -> level.sendParticles(
                            BendingTheme.particle(Config.ESSTREAM_GUST_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                            pl.x,
                            pl.y,
                            pl.z,
                            Config.ESSTREAM_GUST_PARTICLE_COUNT.get(),
                            0.08,
                            0.08,
                            0.08,
                            0.008);
                    case 2 -> level.sendParticles(water, pl.x, pl.y, pl.z, 1, 0, 0, 0, 0);
                    case 3 -> level.sendParticles(earth, pl.x, pl.y, pl.z, 1, 0, 0, 0, 0);
                    default -> {}
                }
            }
        }

        if (!this.debris.isEmpty() && ++this.debrisAge > 25) {
            clearDebris();
        }
        return true;
    }

    private void impact(ServerPlayer sp) {
        SphereBlast.crater(level, this.pos, CRATER, REVERT_MS);
        for (Entity entity : level.getEntities(sp, new AABB(pos, pos).inflate(CRATER + 1))) {
            if (entity.getUUID().equals(sp.getUUID())
                    || !(entity instanceof LivingEntity living)
                    || entity instanceof ArmorStand) {
                continue;
            }
            living.setDeltaMovement(this.dir.scale(KNOCKBACK));
            living.hurtMarked = true;
            living.hurt(sp.damageSources().magic(), (float) DAMAGE);
        }
        level.sendParticles(
                BendingTheme.particle(Config.ESSTREAM_IMPACT_PARTICLE.get(), owner, ParticleTypes.FLAME),
                pos.x,
                pos.y,
                pos.z,
                Config.ESSTREAM_IMPACT_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.2);
        level.sendParticles(
                BendingTheme.particle(Config.ESSTREAM_IMPACT_SMOKE_PARTICLE.get(), ParticleTypes.LARGE_SMOKE),
                pos.x,
                pos.y,
                pos.z,
                Config.ESSTREAM_IMPACT_SMOKE_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.2);
        level.sendParticles(
                BendingTheme.particle(Config.ESSTREAM_SPARK_PARTICLE.get(), ParticleTypes.FIREWORK),
                pos.x,
                pos.y,
                pos.z,
                Config.ESSTREAM_SPARK_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.2);
        level.sendParticles(
                BendingTheme.particle(Config.ESSTREAM_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                pos.x,
                pos.y,
                pos.z,
                Config.ESSTREAM_BLAST_PARTICLE_COUNT.get(),
                0.3,
                0.3,
                0.3,
                0.1);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.0F);
        for (int i = 0; i < 4; i++) {
            BlockPos at = BlockPos.containing(pos).above();
            FallingBlockEntity fb = FallingBlockEntity.fall(level, at, Blocks.DIRT.defaultBlockState());
            Vec3 toss = new Vec3((level.random.nextDouble() - 0.5), 1.0, (level.random.nextDouble() - 0.5))
                    .normalize()
                    .scale(1.5);
            fb.setDeltaMovement(toss);
            fb.hurtMarked = true;
            this.debris.add(fb);
        }
        this.debrisAge = 0;
    }

    private void clearDebris() {
        for (FallingBlockEntity fb : this.debris) {
            if (!fb.isRemoved()) {
                fb.discard();
            }
        }
        this.debris.clear();
    }

    private static Vec3 orthogonal(Vec3 dir, double angleDeg, double radius) {
        Vec3 n = dir.normalize();
        Vec3 up = Math.abs(n.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 u = n.cross(up).normalize();
        Vec3 v = n.cross(u).normalize();
        double a = Math.toRadians(angleDeg);
        return u.scale(Math.cos(a) * radius).add(v.scale(Math.sin(a) * radius));
    }

    @Override
    public void onRemove() {
        clearDebris();
    }
}
