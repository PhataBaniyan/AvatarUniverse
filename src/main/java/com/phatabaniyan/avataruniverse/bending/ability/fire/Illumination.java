package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra Illumination (modern light-block path): click to carry
 * a firebender's glow that follows the eyes as a fake LIGHT block (per-viewer
 * packets, world untouched) plus a hand flame wisp every 3 ticks. Starts only
 * in darkness below the threshold with a free hand, dies in water, reverts
 * its fake block on move/remove. Reference values: Cooldown 1000ms,
 * Threshold 7, Light 14.
 */
public class Illumination extends BendingAbility {
    public static final String ID = "Illumination";

    /** Reference Cooldown 1000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ILLUMINATION_COOLDOWN_MS.get());

    private static final int THRESHOLD = Config.ILLUMINATION_THRESHOLD.get();
    private static final int LIGHT_LEVEL = Config.ILLUMINATION_LIGHT_LEVEL.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private final boolean dead;
    private int ticks;
    private BlockPos fake;

    public Illumination(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        boolean invalid = false;
        Illumination old = BendingManager.find(player.getUUID(), Illumination.class);
        if (old != null) {
            BendingManager.remove(old);
            invalid = true;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (!invalid && bending != null && bending.isOnCooldown(ID, level.getGameTime())) {
            invalid = true;
        }
        int here = invalid
                ? Integer.MAX_VALUE
                : level.getMaxLocalRawBrightness(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ()));
        if (!invalid && (here >= THRESHOLD || !slotsFree(player))) {
            invalid = true;
        }
        if (!invalid && bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
        this.dead = invalid;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (dead || !alive(player) || !gate(owner)) {
            return false;
        }
        // Water kills the glow, like upstream.
        BlockPos feet = player.blockPosition();
        if (!level.getFluidState(feet).isEmpty() || player.isInWater()) {
            return false;
        }
        if (!slotsFree(player)) {
            return false;
        }
        this.ticks++;
        if (this.ticks % 3 == 0) {
            Vec3 hand = handPos(player);
            level.sendParticles(
                    BendingTheme.particle(Config.ILLUMINATION_WISP_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    hand.x,
                    hand.y,
                    hand.z,
                    Config.ILLUMINATION_WISP_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.0);
        }
        return updateLight();
    }

    private static boolean slotsFree(ServerPlayer player) {
        return player.getMainHandItem().isEmpty() || player.getOffhandItem().isEmpty();
    }

    private static Vec3 handPos(ServerPlayer sp) {
        Vec3 eye = new Vec3(sp.getX(), sp.getEyeY(), sp.getZ());
        Vec3 look = sp.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        // Free hand side: main hand is right by default.
        double s = sp.getMainHandItem().isEmpty() ? 0.55 : -0.55;
        side = side.normalize().scale(s);
        return new Vec3(eye.x + side.x, eye.y - 0.3, eye.z + side.z);
    }

    private boolean updateLight() {
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        BlockPos at = eye;
        int lvl = LIGHT_LEVEL;
        if (!level.getBlockState(eye).isAir() && (this.fake == null || !this.fake.equals(eye))) {
            boolean found = false;
            for (Direction dir : Direction.values()) {
                BlockPos nb = eye.relative(dir);
                if (!level.isLoaded(nb)) {
                    continue;
                }
                if (level.getBlockState(nb).isAir() || (this.fake != null && this.fake.equals(nb))) {
                    at = nb.immutable();
                    lvl = Math.max(1, LIGHT_LEVEL - 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        BlockState fakeState =
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, Math.max(1, Math.min(15, lvl)));
        if (!at.equals(this.fake)) {
            revertFake();
            int here =
                    level.getMaxLocalRawBrightness(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ()));
            if (here > THRESHOLD) {
                return false;
            }
            this.fake = at.immutable();
            sendToAll(level, this.fake, fakeState);
        } else if (this.ticks % 10 == 0) {
            // Refresh for late-joining viewers, like the source.
            sendToAll(level, this.fake, level.getBlockState(this.fake));
            sendToAll(level, this.fake, fakeState);
        }
        return true;
    }

    private void revertFake() {
        if (this.fake == null) {
            return;
        }
        sendToAll(level, this.fake, level.getBlockState(this.fake));
        this.fake = null;
    }

    private static void sendToAll(ServerLevel level, BlockPos pos, BlockState state) {
        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(pos, state);
        for (ServerPlayer viewer : level.players()) {
            viewer.connection.send(packet);
        }
    }

    @Override
    public void onRemove() {
        revertFake();
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }
}
