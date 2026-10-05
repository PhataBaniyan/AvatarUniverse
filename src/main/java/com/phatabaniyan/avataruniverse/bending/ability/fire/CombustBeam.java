package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code CombustBeam}: hold sneak to charge the
 * forehead beam from a thread to a torrent — power, damage, and steering all
 * grow with the charge percentage. Release to fire; taking a hit mid-charge
 * (polled via health snapshot, no event hook) detonates it in your own face,
 * weakened. The blast hurts entities only and never breaks blocks (no
 * {@code level.explode}, matching the source). Reference values: Cooldown
 * 6000ms (120 ticks), MinCharge 1000ms (20 ticks), MaxCharge 3000ms
 * (60 ticks), Range 25, MinPower 1, MaxPower 4, MinDamage 3, MaxDamage 9,
 * MaxAngle 15.
 */
public class CombustBeam extends BendingAbility {
    public static final String ID = "CombustBeam";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.COMBUSTBEAM_COOLDOWN_TICKS.get();
    /** Reference MinCharge 1000ms, in server ticks. */
    private static final long MIN_CHARGE_TICKS = Config.COMBUSTBEAM_MIN_CHARGE_TICKS.get();
    /** Reference MaxCharge 3000ms, in server ticks. */
    private static final long MAX_CHARGE_TICKS = Config.COMBUSTBEAM_MAX_CHARGE_TICKS.get();

    private static final double RANGE = Config.COMBUSTBEAM_RANGE.get();
    private static final double MIN_POWER = Config.COMBUSTBEAM_MIN_POWER.get();
    private static final double MAX_POWER = Config.COMBUSTBEAM_MAX_POWER.get();
    private static final double MIN_DAMAGE = Config.COMBUSTBEAM_MIN_DAMAGE.get();
    private static final double MAX_DAMAGE = Config.COMBUSTBEAM_MAX_DAMAGE.get();
    private static final double MAX_ANGLE = Config.COMBUSTBEAM_MAX_ANGLE.get();
    private static final double BLAST_BASE_RADIUS = Config.COMBUSTBEAM_BLAST_BASE_RADIUS.get();
    private static final double KNOCKBACK_CAP = Config.COMBUSTBEAM_KNOCKBACK_CAP.get();
    private static final int IGNITE_SECONDS = Config.COMBUSTBEAM_IGNITE_SECONDS.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private final double startHealth;
    private boolean charged = false;
    private boolean firing = false;
    private double power = MIN_POWER;
    private double damage = MIN_DAMAGE;
    private double steer = MAX_ANGLE;
    private Vec3 pos;
    private Vec3 dir;
    private Vec3 origin;

    public CombustBeam(ServerPlayer player) {
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
        if (!this.firing) {
            if (!player.isShiftKeyDown()) {
                if (!this.charged) {
                    return false;
                }
                this.pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
                this.origin = this.pos;
                this.dir = player.getLookAngle().normalize();
                this.firing = true;
                level.playSound(
                        null,
                        this.pos.x,
                        this.pos.y,
                        this.pos.z,
                        SoundEvents.FIREWORK_ROCKET_BLAST,
                        SoundSource.PLAYERS,
                        0.7F,
                        0.9F);
                return true;
            }
            long elapsed = level.getGameTime() - this.startTime;
            if (player.getHealth() < this.startHealth) {
                // Interrupted: it goes off in your face, weakened.
                this.power = MIN_POWER;
                this.damage = MIN_DAMAGE;
                this.pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
                explode(true);
                return false;
            }
            double percent = Math.min(
                    1.0,
                    (double) Math.max(0, elapsed - MIN_CHARGE_TICKS)
                            / (double) Math.max(1, MAX_CHARGE_TICKS - MIN_CHARGE_TICKS));
            if (elapsed >= MIN_CHARGE_TICKS) {
                this.charged = true;
            }
            this.power = MIN_POWER + (MAX_POWER - MIN_POWER) * percent;
            this.damage = MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * percent;
            this.steer = MAX_ANGLE * (1.0 - percent * 0.7);
            Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTBEAM_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    eye.x,
                    eye.y,
                    eye.z,
                    Config.COMBUSTBEAM_FLAME_PARTICLE_COUNT.get(),
                    0.25,
                    0.25,
                    0.25,
                    0.03);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 5, false, false, false));
            if (elapsed >= MAX_CHARGE_TICKS && ((int) elapsed) % 20 == 0) {
                player.displayClientMessage(Component.literal("100%"), true);
            } else if (this.charged) {
                player.displayClientMessage(Component.literal((int) (percent * 100) + "%"), true);
            }
            return true;
        }
        if (player.isShiftKeyDown()) {
            Vec3 want = player.getLookAngle().normalize();
            double maxTurn = Math.toRadians(this.steer) / 20.0;
            Vec3 blended = this.dir.add(want.subtract(this.dir).scale(Math.min(1.0, maxTurn * 20)));
            if (blended.lengthSqr() > 1.0e-6) {
                this.dir = blended.normalize();
            }
        }
        for (int j = 0; j < (int) Math.max(1, Math.round(this.power)); j++) {
            this.pos = this.pos.add(this.dir);
            if (this.pos.distanceToSqr(this.origin) > RANGE * RANGE) {
                explode(false);
                return false;
            }
            BlockPos bp = BlockPos.containing(this.pos);
            if (!level.isLoaded(bp)) {
                explode(false);
                return false;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                explode(false);
                return false;
            }
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTBEAM_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    Config.COMBUSTBEAM_FLAME_PARTICLE_COUNT.get(),
                    0.15,
                    0.15,
                    0.15,
                    0.03);
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTBEAM_SMOKE_PARTICLE.get(), ParticleTypes.SMOKE),
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    Config.COMBUSTBEAM_SMOKE_PARTICLE_COUNT.get(),
                    0.15,
                    0.15,
                    0.15,
                    0.03);
            for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(1.0))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                explode(false);
                return false;
            }
        }
        return true;
    }

    private void explode(boolean self) {
        Vec3 at = this.pos == null ? player.position().add(0, 1, 0) : this.pos;
        double radius = BLAST_BASE_RADIUS + this.power;
        for (Entity e : level.getEntities(player, new AABB(at, at).inflate(radius))) {
            if (!self && e.getUUID().equals(player.getUUID())) {
                continue;
            }
            if (e instanceof ArmorStand) {
                continue;
            }
            Vec3 push = e.position().subtract(at);
            if (push.lengthSqr() < 1.0e-4) {
                push = new Vec3(0, 1, 0);
            }
            double dist = Math.sqrt(e.position().distanceToSqr(at));
            double kb = this.power / (0.3 + dist);
            e.setDeltaMovement(push.normalize().scale(Math.min(KNOCKBACK_CAP, kb)));
            e.hurtMarked = true;
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), (float) this.damage);
                living.igniteForSeconds(IGNITE_SECONDS);
            }
        }
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTBEAM_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                at.x,
                at.y,
                at.z,
                Config.COMBUSTBEAM_BLAST_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.1);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTBEAM_BURST_PARTICLE.get(), owner, ParticleTypes.FLAME),
                at.x,
                at.y,
                at.z,
                Config.COMBUSTBEAM_BURST_PARTICLE_COUNT.get(),
                0.6,
                0.6,
                0.6,
                0.08);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.0F);
        cool(owner, level, ID, COOLDOWN_TICKS);
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
