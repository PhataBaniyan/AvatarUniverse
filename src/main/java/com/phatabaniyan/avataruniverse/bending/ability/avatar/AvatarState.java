package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Port of ProjectAvatar {@code AvatarState}: click to enter, click again to
 * release (constructor toggle like FireSki/AirSpout). While active the holder
 * glows, regenerates, resists damage and moves faster, trailing end-rod
 * motes. Releasing (or dying/logging out/losing the element) plays the
 * extinguish sound and applies a cooldown proportional to time served:
 * max 60000ms, min 3000ms, inside/4.
 */
public class AvatarState extends BendingAbility {
    public static final String ID = "AvatarState";

    /** Reference avatar damage_factor 2.0 (source BendingConfig default). */
    private static final double DAMAGE_FACTOR = Config.AVATARSTATE_DAMAGE_FACTOR.get();
    /** Reference avatar range_factor 1.5 (source BendingConfig default). */
    private static final double RANGE_FACTOR = Config.AVATARSTATE_RANGE_FACTOR.get();

    private final ServerLevel level;
    private boolean started = false;

    public AvatarState(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        AvatarState old = BendingManager.find(player.getUUID(), AvatarState.class);
        if (old != null) {
            BendingManager.remove(old);
            return;
        }
        this.started = true;
        player.serverLevel()
                .playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.TOTEM_USE,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.0F);
    }

    /** Live-state query used by other avatar abilities (backed by BendingManager.find). */
    public static boolean isActive(ServerPlayer player) {
        return player != null && BendingManager.find(player.getUUID(), AvatarState.class) != null;
    }

    public static double scaleDamage(ServerPlayer player, double value) {
        if (isActive(player)) {
            return value * DAMAGE_FACTOR;
        }
        return value;
    }

    public static double scaleRange(ServerPlayer player, double value) {
        if (isActive(player)) {
            return value * RANGE_FACTOR;
        }
        return value;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!started) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        player.setGlowingTag(true);
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                Config.AVATARSTATE_EFFECT_DURATION_TICKS.get(),
                Config.AVATARSTATE_REGEN_AMP.get(),
                false,
                false,
                false));
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                Config.AVATARSTATE_EFFECT_DURATION_TICKS.get(),
                Config.AVATARSTATE_RESIST_AMP.get(),
                false,
                false,
                false));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                Config.AVATARSTATE_EFFECT_DURATION_TICKS.get(),
                Config.AVATARSTATE_SPEED_AMP.get(),
                false,
                false,
                false));
        level.sendParticles(
                BendingTheme.particle(Config.AVATARSTATE_MAIN_PARTICLE.get(), ParticleTypes.END_ROD),
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                Config.AVATARSTATE_MAIN_PARTICLE_COUNT.get(),
                0.6,
                0.8,
                0.6,
                0.02);
        return true;
    }

    @Override
    public void onRemove() {
        if (!started) {
            return;
        }
        started = false;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            player.setGlowingTag(false);
            player.serverLevel()
                    .playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.PLAYERS,
                            0.4F,
                            1.2F);
        }
        long insideMs = (level.getGameTime() - startTime) * 50L;
        long cdMs = Math.min(
                Config.AVATARSTATE_COOLDOWN_MAX_MS.get(),
                Math.max(
                        Config.AVATARSTATE_COOLDOWN_MIN_MS.get(),
                        insideMs / Config.AVATARSTATE_COOLDOWN_DIVISOR.get()));
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + cdMs / 50L);
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AVATAR) && bending.isToggled();
    }
}
