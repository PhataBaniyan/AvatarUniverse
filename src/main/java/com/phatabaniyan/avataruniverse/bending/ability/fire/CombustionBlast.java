package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code CombustionBlast} (JedCore's Combustion): hold
 * sneak to charge the ring, release to loose a guided spark-beam that follows
 * your gaze until something ends it. Taking a hit while charging (polled via
 * health snapshot, no event hook) misfires it on yourself. Flying past range
 * fizzles with no explosion and no cooldown. Reference values: Cooldown
 * 5000ms (100 ticks), Charge 2000ms (40 ticks), Damage 5, BlastRadius 4,
 * Range 25, vanilla explosion power 3.0 with block damage.
 */
public class CombustionBlast extends BendingAbility {
    public static final String ID = "CombustionBlast";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.COMBUSTIONBLAST_COOLDOWN_MS.get());
    /** Reference Charge 2000ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.COMBUSTIONBLAST_CHARGE_MS.get());

    private static final double DAMAGE = Config.COMBUSTIONBLAST_DAMAGE.get();
    private static final double BLAST_RADIUS = Config.COMBUSTIONBLAST_BLAST_RADIUS.get();
    private static final double RANGE = Config.COMBUSTIONBLAST_RANGE.get();
    private static final double HIT_RADIUS = Config.COMBUSTIONBLAST_HIT_RADIUS.get();
    private static final double EXPLOSION_POWER = Config.COMBUSTIONBLAST_EXPLOSION_POWER.get();
    private static final int IGNITE_SECONDS = Config.COMBUSTIONBLAST_IGNITE_MS.get() / 1000;

    private final ServerPlayer player;
    private final ServerLevel level;
    private final double startHealth;
    private boolean charged = false;
    private boolean flying = false;
    private Vec3 pos;
    private double travelled = 0;
    private double ringAngle = 0;

    public CombustionBlast(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.startHealth = player.getHealth();
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
        if (!this.flying) {
            if (player.isShiftKeyDown()) {
                if (player.getHealth() < this.startHealth) {
                    explodeAt(player.position().add(0, 1, 0));
                    cool(owner, level, ID, COOLDOWN_TICKS);
                    return false;
                }
                if (!this.charged && level.getGameTime() - this.startTime >= CHARGE_TICKS) {
                    this.charged = true;
                }
                chargeRing();
                return true;
            }
            if (!this.charged) {
                return false;
            }
            this.pos = new Vec3(player.getX(), player.getEyeY() - 0.8, player.getZ());
            this.flying = true;
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.8F);
            return true;
        }
        Vec3 dir = player.getLookAngle().normalize();
        this.travelled += 1.0;
        if (this.travelled >= RANGE) {
            return false;
        }
        this.pos = this.pos.add(dir);
        BlockPos bp = BlockPos.containing(this.pos);
        if (!level.isLoaded(bp)) {
            return false;
        }
        var state = level.getBlockState(bp);
        if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
            explodeAt(this.pos);
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(HIT_RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            explodeAt(this.pos);
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTIONBLAST_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.COMBUSTIONBLAST_FLAME_PARTICLE_COUNT.get(),
                0.08,
                0.08,
                0.08,
                0.03);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTIONBLAST_TRAIL_PARTICLE.get(), ParticleTypes.SMOKE),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.COMBUSTIONBLAST_TRAIL_PARTICLE_COUNT.get(),
                0.12,
                0.12,
                0.12,
                0.04);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTIONBLAST_SPARK_PARTICLE.get(), ParticleTypes.FIREWORK),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.COMBUSTIONBLAST_SPARK_PARTICLE_COUNT.get(),
                0.08,
                0.08,
                0.08,
                0.04);
        return true;
    }

    private void chargeRing() {
        Vec3 base = player.position().add(0, 1, 0);
        this.ringAngle += 0.5;
        for (int k = 0; k < 12; k++) {
            double a = this.ringAngle + k * (Math.PI / 6);
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTIONBLAST_RING_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    base.x + Math.cos(a) * 1.75,
                    base.y,
                    base.z + Math.sin(a) * 1.75,
                    Config.COMBUSTIONBLAST_RING_PARTICLE_COUNT.get(),
                    0.02,
                    0.02,
                    0.02,
                    0.01);
        }
        if (this.charged) {
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTIONBLAST_CHARGE_PARTICLE.get(), ParticleTypes.SMOKE),
                    base.x,
                    base.y,
                    base.z,
                    Config.COMBUSTIONBLAST_CHARGE_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.04);
        }
    }

    private void explodeAt(Vec3 at) {
        for (Entity e : level.getEntities(player, new AABB(at, at).inflate(BLAST_RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), (float) DAMAGE);
                living.igniteForSeconds(IGNITE_SECONDS);
            }
        }
        // Small TNT-like pop where it lands.
        level.explode(
                null,
                at.x,
                at.y,
                at.z,
                (float) EXPLOSION_POWER,
                net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTIONBLAST_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                at.x,
                at.y,
                at.z,
                Config.COMBUSTIONBLAST_BLAST_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.1);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTIONBLAST_BURST_PARTICLE.get(), owner, ParticleTypes.FLAME),
                at.x,
                at.y,
                at.z,
                Config.COMBUSTIONBLAST_BURST_PARTICLE_COUNT.get(),
                0.7,
                0.7,
                0.7,
                0.08);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.9F, 0.9F);
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
