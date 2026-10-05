package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
 * Port of ProjectAvatar {@code SonicBlast}: hold sneak to charge the scream,
 * release to loose a fast ring-wave that leaves nausea and blindness where
 * it connects.
 * Reference values: Cooldown 3000ms (60 ticks), Warmup 1500ms (30 ticks),
 * Damage 4, Range 20, HitRadius 1.5, Nausea 100 ticks, Blind 60 ticks.
 */
public class SonicBlast extends BendingAbility {
    public static final String ID = "SonicBlast";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.SONICBLAST_COOLDOWN_MS.get());
    /** Reference warmup 1500ms, in server ticks. */
    private static final long WARMUP_TICKS = Config.msToTicks(Config.SONICBLAST_WARMUP_MS.get());

    private static final double DAMAGE = Config.SONICBLAST_DAMAGE.get();
    private static final double RANGE = Config.SONICBLAST_RANGE.get();
    private static final double HIT_RADIUS = Config.SONICBLAST_HIT_RADIUS.get();
    private static final int NAUSEA_TICKS = Config.msToTicks(Config.SONICBLAST_NAUSEA_MS.get());
    private static final int BLIND_TICKS = Config.msToTicks(Config.SONICBLAST_BLIND_MS.get());
    private static final int NAUSEA_AMP = Config.SONICBLAST_NAUSEA_AMPLIFIER.get();
    private static final int BLIND_AMP = Config.SONICBLAST_BLIND_AMPLIFIER.get();
    private static final double KNOCKBACK = Config.SONICBLAST_KNOCKBACK.get();

    private final ServerLevel level;
    private boolean charged = false;
    private boolean flying = false;
    private Vec3 pos;
    private Vec3 dir;
    private double travelled = 0;
    private double ringAngle = 0;
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public SonicBlast(ServerPlayer player) {
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
        if (!this.flying) {
            if (player.isShiftKeyDown()) {
                this.dir = player.getLookAngle().normalize();
                if (!this.charged && level.getGameTime() - this.startTime >= WARMUP_TICKS) {
                    this.charged = true;
                }
                if (this.charged) {
                    Vec3 eye = eye(player);
                    level.sendParticles(
                            BendingTheme.particle(Config.SONICBLAST_CHARGE_PARTICLE.get(), ParticleTypes.NOTE),
                            eye.x,
                            eye.y,
                            eye.z,
                            Config.SONICBLAST_CHARGE_PARTICLE_COUNT.get(),
                            0.4,
                            0.4,
                            0.4,
                            0.05);
                }
                return true;
            }
            if (!this.charged) {
                return false;
            }
            this.pos = eye(player);
            this.flying = true;
            cool(owner, level, ID, COOLDOWN_TICKS);
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS,
                    0.7F,
                    1.4F);
            return true;
        }
        for (int i = 0; i < 5; i++) {
            this.travelled += 0.2;
            if (this.travelled >= RANGE) {
                return false;
            }
            this.pos = this.pos.add(this.dir.scale(0.2));
            BlockPos bp = BlockPos.containing(this.pos);
            if (!level.isLoaded(bp)) {
                return false;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                return false;
            }
            drawRing();
            for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(HIT_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                    living.hurt(player.damageSources().magic(), (float) DAMAGE);
                    living.addEffect(
                            new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS, NAUSEA_AMP, false, false, false));
                    living.addEffect(
                            new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS, BLIND_AMP, false, false, false));
                }
                e.setDeltaMovement(this.dir.scale(KNOCKBACK));
                e.hurtMarked = true;
                return false;
            }
        }
        return true;
    }

    private void drawRing() {
        Vec3 side = new Vec3(-this.dir.z, 0, this.dir.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        Vec3 up = side.cross(this.dir).normalize();
        this.ringAngle += 0.4;
        for (int k = 0; k < 18; k++) {
            double a = this.ringAngle + k * (Math.PI / 9);
            Vec3 p = this.pos.add(side.scale(Math.cos(a))).add(up.scale(Math.sin(a)));
            level.sendParticles(
                    BendingTheme.particle(Config.SONICBLAST_RING_PARTICLE.get(), ParticleTypes.NOTE),
                    p.x,
                    p.y,
                    p.z,
                    Config.SONICBLAST_RING_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
        }
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
