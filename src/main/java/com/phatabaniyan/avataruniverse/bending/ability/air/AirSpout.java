package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of ProjectAvatar {@code AirSpout}: click to ride a toggleable column
 * of air that sustains near the ground and lifts via vanilla flight, drawing
 * a gust spiral up the shaft. Clicking again steps off. Reference values:
 * Cooldown 5000ms (100 ticks), Duration 0 (infinite), Height 16, Interval
 * 100ms (2 ticks).
 */
public class AirSpout extends BendingAbility {
    public static final String ID = "AirSpout";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.AIRSPOUT_COOLDOWN_TICKS.get();
    /** Reference Duration 0 (infinite), in server ticks. */
    private static final int DURATION_TICKS = Config.AIRSPOUT_DURATION_TICKS.get();

    private static final double HEIGHT = Config.AIRSPOUT_HEIGHT.get();
    /** Reference Interval 100ms, in server ticks. */
    private static final int INTERVAL_TICKS = Config.AIRSPOUT_INTERVAL_TICKS.get();

    private static final double THRESHOLD = Config.AIRSPOUT_THRESHOLD.get();

    private final ServerLevel level;
    private final long startTick;
    private long lastAnim = 0;
    private boolean prevMayfly = false;
    private boolean prevFlying = false;
    private boolean started = false;
    private boolean dead = false;
    private int tick = 0;
    private int groundTick = -10;
    private BlockPos groundCache = null;

    public AirSpout(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.startTick = level.getGameTime();

        AirSpout old = BendingManager.find(player.getUUID(), AirSpout.class);
        if (old != null) {
            BendingManager.remove(old);
            this.dead = true;
            return;
        }

        if (!withinHeight(player, THRESHOLD)) {
            this.dead = true;
            return;
        }

        this.prevMayfly = player.getAbilities().mayfly;
        this.prevFlying = player.getAbilities().flying;
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    private boolean withinHeight(ServerPlayer sp, double threshold) {
        BlockPos ground = getGround(sp);
        if (ground == null) {
            return false;
        }
        return sp.getY() <= ground.getY() + HEIGHT + threshold;
    }

    private BlockPos getGround(ServerPlayer sp) {
        BlockPos feet = sp.blockPosition();
        for (int i = 0; i <= (int) HEIGHT + 5; i++) {
            BlockPos p = feet.below(i);
            BlockState s = level.getBlockState(p);
            if (s.isSolidRender(level, p) || !s.getFluidState().isEmpty()) {
                return p.immutable();
            }
        }
        return null;
    }

    @Override
    public boolean progress() {
        if (dead || !started) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || !player.isAlive() || player.hasDisconnected()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        long gameTime = level.getGameTime();
        if (DURATION_TICKS > 0 && gameTime > this.startTick + DURATION_TICKS) {
            cool(owner);
            return false;
        }
        // Single ground lookup per tick (was two), cached for 3 ticks.
        this.tick++;
        if (this.tick - this.groundTick >= 3) {
            this.groundTick = this.tick;
            this.groundCache = getGround(player);
        }
        BlockPos ground = this.groundCache;
        if (ground == null || player.getY() > ground.getY() + HEIGHT + THRESHOLD) {
            cool(owner);
            return false;
        }
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        BlockState eyeState = level.getBlockState(eye);
        if (eyeState.isSolidRender(level, eye) || player.isEyeInFluid(FluidTags.WATER)) {
            return false;
        }

        player.resetFallDistance();
        player.setSprinting(false);
        if (player.getRandom().nextInt(4) == 0) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.WIND_CHARGE_BURST,
                    SoundSource.PLAYERS,
                    0.25F,
                    1.4F);
        }

        double dy = player.getY() - ground.getY();
        if (dy > HEIGHT) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        } else if (!player.getAbilities().flying) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        if (gameTime >= lastAnim + INTERVAL_TICKS) {
            lastAnim = gameTime;
            double top = Math.min(player.getY(), ground.getY() + HEIGHT);
            for (double y = ground.getY() + 1; y <= top; y += 1) {
                level.sendParticles(
                        BendingTheme.particle(Config.AIRSPOUT_COLUMN_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        player.getX(),
                        y,
                        player.getZ(),
                        Config.AIRSPOUT_COLUMN_PARTICLE_COUNT.get(),
                        0.4,
                        0.4,
                        0.4,
                        0.02);
            }
        }
        return true;
    }

    private void cool(UUID id) {
        BendingPlayer bending = BendingPlayer.get(id);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
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
            player.resetFallDistance();
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }
}
