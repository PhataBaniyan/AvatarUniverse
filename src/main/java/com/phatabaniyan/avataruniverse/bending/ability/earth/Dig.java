package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code Dig} (sneak to burrow: clears earth around the
 * gaze, glides along the look vector, keeps night vision topped up).
 * Reference values: Cooldown 3000ms, Duration -1 (no expiry), RevertTime
 * 3500ms, Speed 0.51. Temp air reverts after 70 ticks via the shared revert
 * queue, matching {@code setRevertTime}.
 */
public class Dig extends EarthAbility {
    public static final String ID = "Dig";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.DIG_COOLDOWN_TICKS.get();
    // Reference Duration -1: no time expiry while sneaking.
    /** Reference RevertTime 3500ms, in server ticks. */
    private static final long REVERT_TICKS = Config.DIG_REVERT_TICKS.get();

    private static final double SPEED = Config.DIG_SPEED.get();
    private static final double TARGET_RANGE = Config.DIG_TARGET_RANGE.get();
    private static final double CLEAR_RANGE = Config.DIG_CLEAR_RANGE.get();
    private static final double FAIL_PUSH = Config.DIG_FAIL_PUSH.get();

    private boolean started;

    public Dig(ServerPlayer player) {
        super(player);
        this.started = true;
    }

    /** Sneak-start gate: must stand on an earthbendable block. */
    public static boolean canBegin(ServerPlayer player) {
        return Accretion.isEarthbendable(
                player.serverLevel(), player.blockPosition().below());
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (player == null || player.isRemoved() || player.isDeadOrDying() || player.level() != level) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.hasElement(BendingElement.EARTH) || !bending.isToggled()) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            return false;
        }

        BlockPos target = targetedBlock(TARGET_RANGE);
        if (target == null || !Accretion.isEarthbendable(level, target)) {
            Vec3 push = player.getLookAngle().normalize().scale(FAIL_PUSH);
            player.setDeltaMovement(push);
            player.hurtMarked = true;
            return false;
        }

        Vec3 eye = player.getEyePosition();
        int reach = (int) Math.ceil(CLEAR_RANGE);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    BlockPos pos = BlockPos.containing(eye.x + dx, eye.y + dy, eye.z + dz)
                            .immutable();
                    if (pos.distSqr(BlockPos.containing(eye)) > CLEAR_RANGE * CLEAR_RANGE) {
                        continue;
                    }
                    if (Accretion.isEarthbendable(level, pos)) {
                        BlockState state = level.getBlockState(pos);
                        level.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, state),
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                5,
                                0.25,
                                0.25,
                                0.25,
                                0.0);
                        TempBlock temp = new TempBlock(level, pos, Blocks.AIR.defaultBlockState());
                        BendingManager.scheduleRevert(temp, level.getGameTime() + REVERT_TICKS);
                    }
                }
            }
        }

        level.sendParticles(
                BendingTheme.particle(Config.DIG_MAIN_PARTICLE.get(), ParticleTypes.CRIT),
                eye.x,
                eye.y,
                eye.z,
                Config.DIG_MAIN_PARTICLE_COUNT.get(),
                0.6,
                0.6,
                0.6,
                0.0);

        player.startFallFlying();
        Vec3 velocity = player.getLookAngle().normalize().scale(SPEED);
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 5, 1, false, false, false));
        return true;
    }

    /** First non-air block along the gaze, like {@code getTargetBlock}. */
    private BlockPos targetedBlock(double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= range; d += 0.25) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.getBlockState(pos).isAir()) {
                return pos.immutable();
            }
        }
        return null;
    }

    @Override
    public void onRemove() {
        if (started) {
            player.stopFallFlying();
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending != null) {
                bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
            }
        }
    }
}
