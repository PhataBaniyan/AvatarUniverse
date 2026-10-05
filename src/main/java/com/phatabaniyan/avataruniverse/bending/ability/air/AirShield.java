package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirShield}: sneak-hold to spin up a growing
 * sphere of wind that shoves entities away on a 50-degree swirl and snuffs
 * fire around the holder. Tap for a short cooldown, hold for the full one;
 * the shield itself holds as long as sneak does.
 * Reference values: Cooldown 7000ms (140 ticks), Duration 6500ms
 * (130 ticks), MaxRadius 4, InitialRadius 1, Speed 10, Push 1.5,
 * Streams 5, Particles 5, dynamic cooldown on.
 */
public class AirShield extends BendingAbility {
    public static final String ID = "AirShield";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.msToTicks(Config.AIRSHIELD_COOLDOWN_MS.get());
    /** Reference Duration 6500ms, in server ticks (cooldown math only). */
    private static final int DURATION_TICKS = Config.msToTicks(Config.AIRSHIELD_DURATION_MS.get());

    private static final double MAX_RADIUS = Config.AIRSHIELD_MAX_RADIUS.get();
    private static final double INITIAL_RADIUS = Config.AIRSHIELD_INITIAL_RADIUS.get();
    private static final double SPEED = Config.AIRSHIELD_SPEED.get();
    private static final double PUSH_FACTOR = Config.AIRSHIELD_PUSH_FACTOR.get();
    private static final int STREAMS = Config.AIRSHIELD_STREAMS.get();
    private static final int PARTICLES = Config.AIRSHIELD_PARTICLES.get();
    private static final double GROWTH = Config.AIRSHIELD_GROWTH.get();
    private static final int PUSH_ANGLE_DEGREES = Config.AIRSHIELD_PUSH_ANGLE_DEGREES.get();
    private static final double PUSH_COS = Math.cos(Math.toRadians(PUSH_ANGLE_DEGREES));
    private static final double PUSH_SIN = Math.sin(Math.toRadians(PUSH_ANGLE_DEGREES));

    private final ServerLevel level;
    private double radius = INITIAL_RADIUS;
    private final Map<Integer, Integer> angles = new HashMap<>();
    private boolean started = false;
    private int tick = 0;

    public AirShield(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        AirShield old = BendingManager.find(player.getUUID(), AirShield.class);
        if (old != null) {
            BendingManager.remove(old);
        }

        int angle = 0;
        int di = Math.max(1, (int) (MAX_RADIUS * 2 / STREAMS));
        for (int i = -(int) MAX_RADIUS + di; i < (int) MAX_RADIUS; i += di) {
            this.angles.put(i, angle);
            angle += 90;
            if (angle == 360) {
                angle = 0;
            }
        }
        // Cooldown starts on press; an early sneak release shortens it.
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null) {
            bending.setCooldown(ID, player.level().getGameTime() + COOLDOWN_TICKS);
        }
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
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (player.isEyeInFluid(FluidTags.WATER)) {
            cool(owner, level, COOLDOWN_TICKS);
            return false;
        }
        if (!player.isShiftKeyDown()) {
            // Tap = short cooldown, siege = full cooldown. No auto-end:
            // the shield holds as long as sneak does.
            long elapsed = level.getGameTime() - this.startTime;
            long reduced = elapsed + Math.max(0L, COOLDOWN_TICKS - DURATION_TICKS);
            cool(owner, level, Math.min(COOLDOWN_TICKS, Math.max(0L, reduced)));
            return false;
        }
        this.tick++;
        rotateShield(player);
        return true;
    }

    private void rotateShield(ServerPlayer player) {
        Vec3 origin = player.position();

        for (Entity entity : level.getEntities(player, new AABB(origin, origin).inflate(this.radius))) {
            if (entity.getUUID().equals(player.getUUID())) {
                continue;
            }
            double distSq = entity.position().distanceToSqr(origin);
            if (distSq <= 4.0 || distSq > this.radius * this.radius) {
                continue;
            }
            double x = entity.getX() - origin.x;
            double z = entity.getZ() - origin.z;
            double mag = Math.sqrt(x * x + z * z);
            if (mag < 1.0e-4) {
                continue;
            }
            double vx = (x * PUSH_COS - z * PUSH_SIN) / mag;
            double vz = (x * PUSH_SIN + z * PUSH_COS) / mag;
            Vec3 v = entity.getDeltaMovement();
            entity.setDeltaMovement(vx * PUSH_FACTOR, v.y, vz * PUSH_FACTOR);
            entity.hurtMarked = true;
            entity.resetFallDistance();
        }

        // Fire-extinguish scan throttled: same outcome, ~1/4 of the block lookups.
        if ((this.tick & 3) == 0) {
            int r = (int) Math.ceil(this.radius);
            BlockPos originPos = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(originPos.offset(-r, -r, -r), originPos.offset(r, r, r))) {
                if (level.getBlockState(pos).is(Blocks.FIRE)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }

        for (var ring : this.angles.entrySet()) {
            int i = ring.getKey();
            if (this.radius <= 0) {
                break;
            }
            double factor = this.radius / MAX_RADIUS;
            double angle = Math.toRadians(ring.getValue());
            double y = origin.y + factor * i;
            double rel = (double) i / this.radius;
            double f = Math.sqrt(Math.max(0.0, 1.0 - factor * factor * rel * rel));

            double x = origin.x + this.radius * Math.cos(angle) * f;
            double z = origin.z + this.radius * Math.sin(angle) * f;

            level.sendParticles(
                    BendingTheme.particle(Config.AIRSHIELD_RING_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    x,
                    y,
                    z,
                    Config.AIRSHIELD_RING_PARTICLE_COUNT.get(),
                    0.15,
                    0.15,
                    0.15,
                    0.01);
            if (ThreadLocalRandom.current().nextInt(20) == 0) {
                level.playSound(null, x, y, z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.25F, 1.5F);
            }

            ring.setValue(ring.getValue() + (int) SPEED);
        }

        if (this.radius < MAX_RADIUS) {
            this.radius += GROWTH;
        }
        if (this.radius > MAX_RADIUS) {
            this.radius = MAX_RADIUS;
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + ticks);
        }
    }
}
