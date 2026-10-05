package com.phatabaniyan.avataruniverse.bending.ability.spiritual;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code SpiritGrasp}: click the ground to root a
 * snaring zone that slows everything inside to a crawl while sapping its
 * strength. Reference values: Cooldown 8000ms, Duration 10000ms, Reach 20,
 * Radius 4, Damage 2.
 */
public class SpiritGrasp extends BendingAbility {
    public static final String ID = "SpiritGrasp";

    /** Reference Cooldown 8000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.SPIRITGRASP_COOLDOWN_TICKS.get();

    private static final double REACH = Config.SPIRITGRASP_REACH.get();
    private static final double RADIUS = Config.SPIRITGRASP_RADIUS.get();
    private static final double DAMAGE = Config.SPIRITGRASP_DAMAGE.get();
    private static final long DURATION_TICKS = Config.SPIRITGRASP_DURATION_TICKS.get();
    private static final int SLOW_DURATION_TICKS = Config.SPIRITGRASP_SLOW_DURATION_TICKS.get();
    private static final int SLOW_AMP = Config.SPIRITGRASP_SLOW_AMP.get();

    private final ServerLevel level;
    private final ServerPlayer player;
    private final long startTick;
    private final Vec3 center;
    private final boolean placed;
    private int tick;
    private double ringAngle;

    public SpiritGrasp(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.startTick = level.getGameTime();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 maintained = eye;
        BlockPos solid = null;
        for (double d = 0.5; d <= REACH; d += 0.5) {
            Vec3 at = eye.add(look.scale(d));
            BlockPos pos = BlockPos.containing(at);
            if (!level.isLoaded(pos)) {
                break;
            }
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                solid = pos.immutable();
                break;
            }
            maintained = at;
        }
        if (solid == null) {
            this.center = maintained;
            this.placed = false;
            return;
        }
        this.center = new Vec3(solid.getX() + 0.5, solid.getY() + 1.0, solid.getZ() + 0.5);
        this.placed = true;
        level.playSound(
                null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.8F);
        cool(player.getUUID(), player, ID, COOLDOWN_TICKS);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!placed || !alive(player) || !gate(owner)) {
            return false;
        }
        if (level.getGameTime() - startTick > DURATION_TICKS) {
            return false;
        }
        tick++;
        ringAngle += 0.4;
        for (int k = 0; k < 8; k++) {
            double a = ringAngle + k * (Math.PI / 4);
            level.sendParticles(
                    BendingTheme.particle(Config.SPIRITGRASP_RING_PARTICLE.get(), ParticleTypes.HAPPY_VILLAGER),
                    center.x + Math.cos(a) * RADIUS,
                    center.y + 0.2,
                    center.z + Math.sin(a) * RADIUS,
                    Config.SPIRITGRASP_RING_PARTICLE_COUNT.get(),
                    0.1,
                    0.1,
                    0.1,
                    0.02);
            if (k % 2 == 0) {
                level.sendParticles(
                        BendingTheme.particle(Config.SPIRITGRASP_INNER_PARTICLE.get(), ParticleTypes.WITCH),
                        center.x + Math.cos(-a) * RADIUS * 0.6,
                        center.y + 0.8,
                        center.z + Math.sin(-a) * RADIUS * 0.6,
                        Config.SPIRITGRASP_INNER_PARTICLE_COUNT.get(),
                        0.1,
                        0.1,
                        0.1,
                        0.02);
            }
        }
        if (tick % 20 == 0) {
            for (Entity entity : level.getEntities(player, new AABB(center, center).inflate(RADIUS, 2, RADIUS))) {
                if (!(entity instanceof LivingEntity living)
                        || entity instanceof ArmorStand
                        || entity.getUUID().equals(owner)) {
                    continue;
                }
                living.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION_TICKS, SLOW_AMP, false, false, false));
                living.hurt(player.damageSources().magic(), (float) DAMAGE);
            }
        }
        return true;
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.SPIRITUAL) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
