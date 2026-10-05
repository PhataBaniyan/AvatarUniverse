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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirSlam} (JedCore combo as a standalone):
 * click to pop a target skyward, then spike it down your gaze half a
 * heartbeat later.
 * Reference values: Cooldown 4000ms (80 ticks), Power 2, Range 15. Pop on
 * click, spike after ~50ms (1 tick), end after ~400ms (8 ticks).
 */
public class AirSlam extends BendingAbility {
    public static final String ID = "AirSlam";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.AIRSLAM_COOLDOWN_TICKS.get();

    private static final double POWER = Config.AIRSLAM_POWER.get();
    private static final double RANGE = Config.AIRSLAM_RANGE.get();
    /** Spike delay ~50ms, in server ticks. */
    private static final int SPIKE_TICKS = Config.AIRSLAM_SPIKE_TICKS.get();
    /** Lifetime ~400ms, in server ticks. */
    private static final int LIFETIME_TICKS = Config.AIRSLAM_LIFETIME_TICKS.get();

    private static final double LIFT = Config.AIRSLAM_LIFT.get();

    private final ServerLevel level;
    private final long startTick;
    private LivingEntity target;
    private boolean spiked = false;
    private boolean started = false;

    public AirSlam(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.startTick = player.level().getGameTime();

        LivingEntity found = acquireTarget(player, RANGE);
        if (found == null) {
            return;
        }
        this.target = found;
        this.target.setDeltaMovement(new Vec3(0, LIFT, 0));
        this.target.hurtMarked = true;
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null) {
            bending.setCooldown(ID, player.level().getGameTime() + COOLDOWN_TICKS);
        }
        level.playSound(
                null,
                found.getX(),
                found.getY(),
                found.getZ(),
                SoundEvents.WIND_CHARGE_BURST,
                SoundSource.PLAYERS,
                0.6F,
                1.0F);
        this.started = true;
    }

    private static LivingEntity acquireTarget(ServerPlayer player, double range) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0; d <= range; d += 0.5) {
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
        long elapsed = level.getGameTime() - this.startTick;
        if (this.target != null && this.target.isAlive() && !this.spiked && elapsed > SPIKE_TICKS) {
            this.spiked = true;
            Vec3 look = player.getLookAngle().normalize();
            this.target.setDeltaMovement(new Vec3(look.x * POWER, 0.05, look.z * POWER));
            this.target.hurtMarked = true;
            this.target.resetFallDistance();
        }
        if (this.target != null) {
            Vec3 t = this.target.position().add(0, 1, 0);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSLAM_TARGET_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    t.x,
                    t.y,
                    t.z,
                    Config.AIRSLAM_TARGET_PARTICLE_COUNT.get(),
                    0.4,
                    0.4,
                    0.4,
                    0.05);
        }
        return elapsed <= LIFETIME_TICKS;
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }
}
