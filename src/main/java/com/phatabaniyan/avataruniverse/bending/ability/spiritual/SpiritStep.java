package com.phatabaniyan.avataruniverse.bending.ability.spiritual;

import com.phatabaniyan.avataruniverse.Config;
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
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code SpiritStep}: click to step to solid ground
 * under the cursor, leaving a wisp burst at both ends of the jaunt.
 * Reference values: Cooldown 6000ms, Range 24.
 */
public class SpiritStep extends BendingAbility {
    public static final String ID = "SpiritStep";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.SPIRITSTEP_COOLDOWN_MS.get());

    private static final double RANGE = Config.SPIRITSTEP_RANGE.get();

    private final ServerLevel level;
    private final boolean stepped;

    public SpiritStep(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 dest = null;
        Vec3 last = eye;
        for (double d = 0.5; d <= RANGE; d += 0.5) {
            Vec3 at = eye.add(look.scale(d));
            BlockPos pos = BlockPos.containing(at);
            if (!level.isLoaded(pos)) {
                break;
            }
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                BlockPos above = pos.above();
                if (level.getBlockState(above).isAir()
                        && level.getBlockState(above.above()).isAir()) {
                    dest = new Vec3(above.getX() + 0.5, above.getY(), above.getZ() + 0.5);
                }
                break;
            }
            last = at;
        }
        if (dest == null) {
            BlockPos p = BlockPos.containing(last);
            if (level.isLoaded(p)
                    && level.getBlockState(p.below()).isSolidRender(level, p.below())
                    && level.getBlockState(p).isAir()
                    && level.getBlockState(p.above()).isAir()) {
                dest = new Vec3(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            }
        }
        if (dest == null) {
            this.stepped = false;
            return;
        }
        burst(level, player.position().add(0, 1, 0));
        player.teleportTo(level, dest.x, dest.y, dest.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        burst(level, dest.add(0, 1, 0));
        level.playSound(null, dest.x, dest.y, dest.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.2F);
        cool(player.getUUID(), player, ID, COOLDOWN_TICKS);
        this.stepped = true;
    }

    private static void burst(ServerLevel level, Vec3 at) {
        level.sendParticles(
                BendingTheme.particle(Config.SPIRITSTEP_BURST_PARTICLE.get(), ParticleTypes.PORTAL),
                at.x,
                at.y,
                at.z,
                Config.SPIRITSTEP_BURST_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.1);
        level.sendParticles(
                BendingTheme.particle(Config.SPIRITSTEP_WISP_PARTICLE.get(), ParticleTypes.END_ROD),
                at.x,
                at.y,
                at.z,
                Config.SPIRITSTEP_WISP_PARTICLE_COUNT.get(),
                0.3,
                0.3,
                0.3,
                0.03);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        // One-shot in the constructor; nothing to sustain.
        return false;
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
