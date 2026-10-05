package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code Catapult}
 * (core/.../earthbending/Catapult.java from the local ProjectKorra-master
 * copy). Sneak on earth/sand/metal to charge through stages with block-dust
 * bursts and a ghast cry per stage; release to launch along the gaze (or
 * straight up when looking up past 45 degrees) with nearby entities thrown
 * too. Click fires an uncharged hop. Cooldown 140t (7000ms).
 */
public class Catapult extends EarthAbility {
    public static final String ID = "Catapult";
    /** Reference StageTimeMult 2.0. */
    private static final double STAGE_TIME_MULT = Config.CATAPULT_STAGE_MULT.get();
    /** Reference Angle 45. */
    private static final double ANGLE_RADIANS = Math.toRadians(Config.CATAPULT_ANGLE_DEG.get());
    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.CATAPULT_COOLDOWN_TICKS.get();

    private static final long HOLD_TICKS = Config.CATAPULT_HOLD_TICKS.get();
    private static final double THROW_RADIUS = Config.CATAPULT_THROW_RADIUS.get();
    private static final int EARTH_DISTANCE = Config.CATAPULT_EARTH_DISTANCE.get();
    private static final int MAX_STAGE = Config.CATAPULT_MAX_STAGE.get();
    private static final double LAUNCH_BONUS = Config.CATAPULT_LAUNCH_BONUS.get();

    private static final Vec3 UP = new Vec3(0, 1, 0);

    private int stage = 1;
    private long stageStartMs;
    private boolean charging;
    private boolean activationHandled;
    private long launchedAt = -1L;
    private BlockState bentState;

    public Catapult(ServerPlayer player, boolean sneak) {
        super(player);
        this.charging = sneak;
        this.stageStartMs = System.currentTimeMillis();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: bendable earth directly below the feet. */
    public static boolean canBegin(ServerPlayer player) {
        return Accretion.isEarthbendable(
                player.serverLevel(), player.blockPosition().below());
    }

    /** Launch immediately (click path): uncharged stage-1 hop. */
    public void launch() {
        charging = false;
    }

    private void moveLaunchedEarth(Vec3 apply, Vec3 direction) {
        Vec3 origin = player.position();
        for (Entity entity : level.getEntitiesOfClass(
                Entity.class,
                new AABB(
                        origin.subtract(THROW_RADIUS, THROW_RADIUS, THROW_RADIUS),
                        origin.add(THROW_RADIUS, THROW_RADIUS, THROW_RADIUS)))) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            entity.setDeltaMovement(apply);
            entity.hurtMarked = true;
        }
        BlockPos source = BlockPos.containing(origin.subtract(direction));
        moveEarth(source, direction, EARTH_DISTANCE, false);
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (launchedAt >= 0) {
            // Launched: hold the raised earth briefly, then revert it.
            return level.getGameTime() - launchedAt < HOLD_TICKS;
        }
        if (!canBegin(player)) {
            return false;
        }
        bentState = level.getBlockState(player.blockPosition().below());

        if (charging) {
            if (stage == MAX_STAGE || !player.isShiftKeyDown()) {
                charging = false;
            } else {
                long wait = (long) (Math.max(0, STAGE_TIME_MULT * (stage - 1)) * 1000);
                if (System.currentTimeMillis() - stageStartMs >= wait) {
                    stage++;
                    stageStartMs = System.currentTimeMillis();
                    Vec3 base = player.position();
                    level.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, bentState),
                            base.x,
                            base.y + 0.2,
                            base.z,
                            15,
                            level.random.nextFloat(),
                            level.random.nextFloat(),
                            level.random.nextFloat(),
                            0.02);
                    level.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, bentState),
                            base.x,
                            base.y + 0.7,
                            base.z,
                            10,
                            level.random.nextFloat(),
                            level.random.nextFloat(),
                            level.random.nextFloat(),
                            0.02);
                    level.playSound(
                            null, base.x, base.y, base.z, SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            return true;
        }

        if (!activationHandled) {
            activationHandled = true;
            Vec3 origin = player.position();
            Vec3 direction = player.getLookAngle().normalize();
            if (angleBetween(UP, player.getLookAngle()) > ANGLE_RADIANS) {
                direction = UP;
            }
            Vec3 target = origin.add(direction.normalize().scale(stage + LAUNCH_BONUS));
            Vec3 apply = target.subtract(origin);
            player.setDeltaMovement(apply);
            player.hurtMarked = true;
            player.resetFallDistance();
            moveLaunchedEarth(apply, direction);
            BlockPos pad = player.blockPosition().below();
            if (Accretion.isEarthbendable(level, pad)) {
                moveEarth(pad, UP, EARTH_DISTANCE, false);
            }
            launchedAt = level.getGameTime();
            cool(owner, player, ID, COOLDOWN_TICKS);
        }
        return launchedAt < 0 || level.getGameTime() - launchedAt < HOLD_TICKS;
    }

    private static double angleBetween(Vec3 a, Vec3 b) {
        double cos = a.dot(b) / Math.max(a.length() * b.length(), 1.0e-9);
        return Math.acos(Math.max(-1.0, Math.min(1.0, cos)));
    }

    @Override
    public void onRemove() {
        revertMovedEarth();
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
