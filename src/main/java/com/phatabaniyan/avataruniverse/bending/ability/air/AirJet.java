package com.phatabaniyan.avataruniverse.bending.ability.air;

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
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirJet}: click to ride a jet of air along the
 * look direction, thrust decaying to half over the flight. Flight is granted
 * for the ride and restored after. Retoggling mid-jet cancels the old ride.
 * Reference values: Duration 2000ms (40 ticks), Speed 0.8, Cooldown 7000ms
 * (140 ticks), gliding overlay on.
 */
public class AirJet extends BendingAbility {
    public static final String ID = "AirJet";

    /** Reference Duration 2000ms, in server ticks. */
    private static final int DURATION_TICKS = Config.AIRJET_DURATION_TICKS.get();
    /** Reference Cooldown 7000ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.AIRJET_COOLDOWN_TICKS.get();

    private static final double SPEED = Config.AIRJET_SPEED.get();
    private static final boolean SHOW_GLIDING = true;

    private final ServerLevel level;
    private long startTick;
    private boolean started = false;
    private boolean prevMayfly;
    private boolean prevFlying;
    private boolean prevGliding;

    public AirJet(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        AirJet old = BendingManager.find(player.getUUID(), AirJet.class);
        if (old != null) {
            BendingManager.remove(old);
            return;
        }

        Vec3 look = player.getLookAngle().normalize().scale(SPEED);
        player.setDeltaMovement(look);
        player.hurtMarked = true;

        this.prevMayfly = player.getAbilities().mayfly;
        this.prevFlying = player.getAbilities().flying;
        this.prevGliding = player.isFallFlying();
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();

        if (SHOW_GLIDING) {
            player.startFallFlying();
        }

        this.startTick = level.getGameTime();
        this.started = true;
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
        if (player == null || !player.isAlive() || player.hasDisconnected()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (player.isInWater()) {
            level.sendParticles(
                    BendingTheme.particle(Config.AIRJET_EXTINGUISH_PARTICLE.get(), ParticleTypes.BUBBLE),
                    player.getX(),
                    player.getY() + 0.5,
                    player.getZ(),
                    Config.AIRJET_EXTINGUISH_PARTICLE_COUNT.get(),
                    0.4,
                    0.4,
                    0.4,
                    0.05);
            return false;
        }
        long gameTime = level.getGameTime();
        if (gameTime > this.startTick + DURATION_TICKS) {
            return false;
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(0.25, 0, 0);
        } else {
            side = side.normalize().scale(0.25);
        }
        double baseY = player.getY() + 0.1;
        level.sendParticles(
                BendingTheme.particle(Config.AIRJET_TRAIL_LEFT_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                player.getX() + side.x,
                baseY,
                player.getZ() + side.z,
                Config.AIRJET_TRAIL_LEFT_PARTICLE_COUNT.get(),
                0.12,
                0.12,
                0.12,
                0.01);
        level.sendParticles(
                BendingTheme.particle(Config.AIRJET_TRAIL_RIGHT_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                player.getX() - side.x,
                baseY,
                player.getZ() - side.z,
                Config.AIRJET_TRAIL_RIGHT_PARTICLE_COUNT.get(),
                0.12,
                0.12,
                0.12,
                0.01);

        double elapsed = gameTime - this.startTick;
        double timefactor = 1.0 - elapsed / (2.0 * DURATION_TICKS);
        Vec3 velocity = look.scale(SPEED * timefactor);
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.resetFallDistance();
        if (SHOW_GLIDING && !player.isFallFlying() && !player.onGround()) {
            player.startFallFlying();
        }
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
            player.getAbilities().mayfly = prevMayfly;
            player.getAbilities().flying = prevFlying;
            player.onUpdateAbilities();
            if (SHOW_GLIDING && !prevGliding) {
                player.stopFallFlying();
            }
            player.resetFallDistance();
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }
}
