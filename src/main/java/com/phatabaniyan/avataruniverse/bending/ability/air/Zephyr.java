package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

/**
 * Port of ProjectAvatar {@code Zephyr}: hold sneak to stir a ring of gentle
 * currents — everything at your level or above drifts down feather-slow,
 * yourself included.
 * Reference values: Cooldown 4000ms (80 ticks), Radius 6.
 */
public class Zephyr extends BendingAbility {
    public static final String ID = "Zephyr";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ZEPHYR_COOLDOWN_MS.get());

    private static final double RADIUS = Config.ZEPHYR_RADIUS.get();
    private static final int SLOW_DURATION_TICKS = Config.msToTicks(Config.ZEPHYR_SLOW_DURATION_MS.get());
    private static final int SLOW_AMPLIFIER = Config.ZEPHYR_SLOW_AMPLIFIER.get();
    private static final int REFRESH_THRESHOLD_TICKS = Config.msToTicks(Config.ZEPHYR_REFRESH_THRESHOLD_MS.get());

    private final ServerLevel level;
    private double ringAngle = 0;

    public Zephyr(ServerPlayer player) {
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
        if (!player.isShiftKeyDown() || player.isEyeInFluid(FluidTags.WATER)) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        for (Entity e : level.getEntities(player, new AABB(player.position(), player.position()).inflate(RADIUS))) {
            if (!(e instanceof LivingEntity living) || e instanceof ArmorStand) {
                continue;
            }
            if (e.position().y < player.getY() - 1.0) {
                continue;
            }
            var slow = living.getEffect(MobEffects.SLOW_FALLING);
            if (slow == null || slow.getDuration() < REFRESH_THRESHOLD_TICKS) {
                living.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, SLOW_DURATION_TICKS, SLOW_AMPLIFIER, false, false, false));
            }
            if (ThreadLocalRandom.current().nextInt(3) == 0) {
                level.sendParticles(
                        BendingTheme.particle(Config.ZEPHYR_DRIFT_PARTICLE.get(), ParticleTypes.CLOUD),
                        e.getX(),
                        e.getY() + 1,
                        e.getZ(),
                        Config.ZEPHYR_DRIFT_PARTICLE_COUNT.get(),
                        0.3,
                        0.3,
                        0.3,
                        0.02);
            }
        }
        var self = player.getEffect(MobEffects.SLOW_FALLING);
        if (self == null || self.getDuration() < REFRESH_THRESHOLD_TICKS) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, SLOW_DURATION_TICKS, SLOW_AMPLIFIER, false, false, false));
        }
        this.ringAngle += 0.3;
        for (int i = 0; i < 6; i++) {
            double a = this.ringAngle + i * (Math.PI / 3);
            level.sendParticles(
                    BendingTheme.particle(Config.ZEPHYR_RING_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    player.getX() + Math.cos(a) * (RADIUS + 0.5),
                    player.getY() + 0.5,
                    player.getZ() + Math.sin(a) * (RADIUS + 0.5),
                    Config.ZEPHYR_RING_PARTICLE_COUNT.get(),
                    0.08,
                    0.08,
                    0.08,
                    0.01);
        }
        player.resetFallDistance();
        return true;
    }

    @Override
    public void onRemove() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            player.resetFallDistance();
        }
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
