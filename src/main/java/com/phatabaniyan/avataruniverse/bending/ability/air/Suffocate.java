package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Suffocate}: sneak-hold channel on a gaze
 * victim. After a charge-up the target is wrapped in spirals of thin air
 * taking damage over time plus slow and blindness. Looking away, releasing
 * sneak, the victim dying/escaping range, or the caster taking a hit breaks
 * the channel (the hit-break is polled from the caster's health each tick).
 * Reference values: Cooldown 2000ms (40 ticks), Charge 2000ms (40 ticks),
 * Range 30, Radius 2, Damage 2, DamageDelay 2000ms (40 ticks),
 * DamageRepeat 1000ms (20 ticks), SlowAmp 1, SlowRepeat 1000ms (20 ticks),
 * SlowDelay 500ms (10 ticks), BlindAmp 0, BlindRepeat 2000ms (40 ticks),
 * BlindDelay 1000ms (20 ticks), AimRadius 2.5.
 */
public class Suffocate extends BendingAbility {
    public static final String ID = "Suffocate";

    /** Reference Cooldown 2000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.SUFFOCATE_COOLDOWN_TICKS.get();
    /** Reference charge 2000ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.SUFFOCATE_CHARGE_TICKS.get();

    private static final double RANGE = Config.SUFFOCATE_RANGE.get();
    private static final double RADIUS = Config.SUFFOCATE_RADIUS.get();
    private static final double DAMAGE = Config.SUFFOCATE_DAMAGE.get();
    private static final long DAMAGE_DELAY_TICKS = Config.SUFFOCATE_DAMAGE_DELAY_TICKS.get();
    private static final long DAMAGE_REPEAT_TICKS = Config.SUFFOCATE_DAMAGE_REPEAT_TICKS.get();
    private static final int SLOW_AMP = Config.SUFFOCATE_SLOW_AMPLIFIER.get();
    private static final long SLOW_REPEAT_TICKS = Config.SUFFOCATE_SLOW_REPEAT_TICKS.get();
    private static final long SLOW_DELAY_TICKS = Config.SUFFOCATE_SLOW_DELAY_TICKS.get();
    private static final int BLIND_AMP = Config.SUFFOCATE_BLIND_AMPLIFIER.get();
    private static final long BLIND_REPEAT_TICKS = Config.SUFFOCATE_BLIND_REPEAT_TICKS.get();
    private static final long BLIND_DELAY_TICKS = Config.SUFFOCATE_BLIND_DELAY_TICKS.get();
    private static final boolean REQUIRE_AIM = true;
    private static final double AIM_RADIUS = Config.SUFFOCATE_AIM_RADIUS.get();
    private static final boolean AFFECT_UNDEAD = false;
    private static final double ANIM_SPEED = 1.0;

    private final ServerLevel level;
    private boolean invalid = false;
    private boolean started = false;
    private long nextDamageAt = 0;
    private long nextSlowAt = 0;
    private long nextBlindAt = 0;
    private double ringAngle = 0;
    private float lastCasterHp;
    private final List<UUID> targets = new ArrayList<>();

    public Suffocate(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.lastCasterHp = player.getHealth();

        LivingEntity target = acquireTarget(player);
        if (target != null) {
            if (AFFECT_UNDEAD || !target.getType().is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
                this.targets.add(target.getUUID());
            }
        }
        if (this.targets.isEmpty()) {
            this.invalid = true;
        }
    }

    @Override
    public String name() {
        return ID;
    }

    private LivingEntity acquireTarget(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = eye(player);
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0; d <= RANGE; d += 0.5) {
            Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            for (Entity e : level.getEntities(player, new AABB(p, p).inflate(0.6))) {
                if (e instanceof LivingEntity living
                        && !e.getUUID().equals(player.getUUID())
                        && !(e instanceof ArmorStand)) {
                    return living;
                }
            }
        }
        return null;
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
        // Hurting the channeler breaks the channel (polled, no event hooks).
        if (sp.getHealth() < this.lastCasterHp) {
            return false;
        }
        this.lastCasterHp = sp.getHealth();

        List<LivingEntity> victims = resolve(sp);
        if (victims.isEmpty()) {
            return false;
        }
        if (REQUIRE_AIM && !victims.isEmpty()) {
            LivingEntity first = victims.get(0);
            Vec3 eye = eye(sp);
            Vec3 look = sp.getLookAngle().normalize();
            double dist = first.getEyePosition().distanceTo(eye);
            Vec3 aim = new Vec3(eye.x + look.x * dist, eye.y + look.y * dist, eye.z + look.z * dist);
            victims.removeIf(t -> t.position().add(0, 1, 0).distanceToSqr(aim) > AIM_RADIUS * AIM_RADIUS);
            this.targets.removeIf(
                    id -> victims.stream().noneMatch(t -> t.getUUID().equals(id)));
            if (victims.isEmpty()) {
                return false;
            }
        }
        long now = level.getGameTime();
        if (now - this.startTime < CHARGE_TICKS) {
            Vec3 eye = eye(sp);
            level.sendParticles(
                    BendingTheme.particle(Config.SUFFOCATE_CHARGE_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    eye.x,
                    eye.y,
                    eye.z,
                    Config.SUFFOCATE_CHARGE_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.02);
            return true;
        }
        if (!this.started) {
            this.started = true;
            this.nextDamageAt = now + DAMAGE_DELAY_TICKS;
            this.nextSlowAt = now + SLOW_DELAY_TICKS;
            this.nextBlindAt = now + BLIND_DELAY_TICKS;
        }
        if (now >= this.nextDamageAt) {
            this.nextDamageAt = now + DAMAGE_REPEAT_TICKS;
            for (LivingEntity t : victims) {
                t.hurt(sp.damageSources().magic(), (float) DAMAGE);
            }
        }
        if (now >= this.nextSlowAt) {
            this.nextSlowAt = now + SLOW_REPEAT_TICKS;
            for (LivingEntity t : victims) {
                t.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int) SLOW_REPEAT_TICKS + 20, SLOW_AMP, false, false, false));
            }
        }
        if (now >= this.nextBlindAt) {
            this.nextBlindAt = now + BLIND_REPEAT_TICKS;
            for (LivingEntity t : victims) {
                t.addEffect(new MobEffectInstance(
                        MobEffects.BLINDNESS, (int) BLIND_REPEAT_TICKS + 20, BLIND_AMP, false, false, false));
                // No air in the lungs: the world spins and the stomach turns.
                t.addEffect(new MobEffectInstance(
                        MobEffects.CONFUSION, (int) BLIND_REPEAT_TICKS + 20, 0, false, false, false));
            }
        }
        for (LivingEntity t : victims) {
            animate(t, now);
        }
        if (!sp.isShiftKeyDown()) {
            return false;
        }
        return true;
    }

    private List<LivingEntity> resolve(ServerPlayer sp) {
        List<LivingEntity> out = new ArrayList<>();
        double rangeSq = RANGE * RANGE;
        for (UUID id : new ArrayList<>(this.targets)) {
            if (level.getEntity(id) instanceof LivingEntity t
                    && t.isAlive()
                    && t.level() == sp.serverLevel()
                    && !(t instanceof ArmorStand)
                    && t.position().distanceToSqr(sp.position()) <= rangeSq) {
                out.add(t);
            } else {
                this.targets.remove(id);
            }
        }
        return out;
    }

    private void animate(LivingEntity target, long now) {
        double elapsed = Math.max(0, now - this.startTime - CHARGE_TICKS);
        double shrink = 1.0 - 0.75 * Math.min(1.0, elapsed / (120.0 * ANIM_SPEED));
        double r = RADIUS * shrink;
        Vec3 eye = target.getEyePosition().add(0, -0.5, 0);
        this.ringAngle += 0.5;
        for (int k = 0; k < 8; k++) {
            double a = this.ringAngle + k * (Math.PI / 4);
            level.sendParticles(
                    BendingTheme.particle(Config.SUFFOCATE_SPIRAL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    eye.x + Math.cos(a) * r,
                    eye.y,
                    eye.z + Math.sin(a) * r,
                    Config.SUFFOCATE_SPIRAL_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
            level.sendParticles(
                    BendingTheme.particle(Config.SUFFOCATE_SPIRAL_MIRROR_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    eye.x - Math.cos(a) * r,
                    eye.y,
                    eye.z - Math.sin(a) * r,
                    Config.SUFFOCATE_SPIRAL_MIRROR_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
            double v = this.ringAngle * 0.7 + k * (Math.PI / 4);
            level.sendParticles(
                    BendingTheme.particle(Config.SUFFOCATE_SPIRAL_UPRIGHT_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    eye.x + Math.sin(v) * r,
                    eye.y + Math.cos(v) * r,
                    eye.z,
                    Config.SUFFOCATE_SPIRAL_UPRIGHT_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
        }
    }

    @Override
    public void onRemove() {
        cool(owner, level, ID, COOLDOWN_TICKS);
    }

    private static Vec3 eye(ServerPlayer player) {
        return new Vec3(player.getX(), player.getEyeY(), player.getZ());
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
