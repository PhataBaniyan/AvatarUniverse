package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Meditate}: hold sneak to gather focus;
 * release after the warmup for speed, jump, and absorption blessings. Taking
 * a hit breaks focus with nothing to show, detected by health polling in
 * progress (no event hooks).
 * Reference values: Warmup 3000ms (60 ticks), Cooldown 10000ms (200 ticks,
 * paid only when the meditation pays off), Boost 60000ms (1200 ticks),
 * Particles 6, Speed/Jump/Absorption amp 1 (applied as 0).
 */
public class Meditate extends BendingAbility {
    public static final String ID = "Meditate";

    /** Reference Warmup 3000ms, in server ticks. */
    private static final long WARMUP_TICKS = Config.MEDITATE_WARMUP_TICKS.get();
    /** Reference Cooldown 10000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.MEDITATE_COOLDOWN_TICKS.get();
    /** Reference Boost 60000ms, in server ticks. */
    private static final long BOOST_TICKS = Config.MEDITATE_BOOST_TICKS.get();

    private static final int PARTICLES = Config.MEDITATE_PARTICLES.get();
    private static final int ABSORPTION_AMP = Config.MEDITATE_ABSORPTION_AMPLIFIER.get();
    private static final int SPEED_AMP = Config.MEDITATE_SPEED_AMPLIFIER.get();
    private static final int JUMP_AMP = Config.MEDITATE_JUMP_AMPLIFIER.get();

    private final ServerLevel level;
    private final float startHealth;

    public Meditate(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.startHealth = player.getHealth();
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
        if (player.getHealth() < this.startHealth) {
            return false;
        }
        Vec3 center = player.position().add(0, 1.0, 0);
        if (level.getGameTime() - this.startTime > WARMUP_TICKS) {
            level.sendParticles(
                    BendingTheme.particle(Config.MEDITATE_BLESSING_PARTICLE.get(), ParticleTypes.WITCH),
                    center.x,
                    center.y,
                    center.z,
                    Config.MEDITATE_BLESSING_PARTICLE_COUNT.get(),
                    0.5,
                    0.5,
                    0.5,
                    0.05);
            if (!player.isShiftKeyDown()) {
                giveBuffs(player);
                cool(owner, level, ID, COOLDOWN_TICKS);
                return false;
            }
            return true;
        }
        if (player.isShiftKeyDown()) {
            level.sendParticles(
                    BendingTheme.particle(Config.MEDITATE_FOCUS_PARTICLE.get(), ParticleTypes.ENCHANT),
                    center.x,
                    center.y,
                    center.z,
                    Config.MEDITATE_FOCUS_PARTICLE_COUNT.get(),
                    0.5,
                    0.5,
                    0.5,
                    0.05);
            return true;
        }
        return false;
    }

    private static void giveBuffs(ServerPlayer player) {
        int ticks = (int) BOOST_TICKS;
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, ticks, Math.max(0, SPEED_AMP - 1), false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, Math.max(0, JUMP_AMP - 1), false, false, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.ABSORPTION, ticks, Math.max(0, ABSORPTION_AMP - 1), false, false, true));
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
