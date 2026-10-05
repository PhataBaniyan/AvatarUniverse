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
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirScooter}: click while sprinting mid-leap to
 * mount a spinning ball of air that carries the rider along the gaze,
 * hovering over the ground below, until crouching, dismounting, stalling, or
 * losing ground beneath. Retoggling mid-ride steps off. Reference values:
 * Speed 0.675, Interval 100ms (2 ticks), Cooldown 500ms (10 ticks), Duration
 * 0 (infinite), MaxHeight 7.
 */
public class AirScooter extends BendingAbility {
    public static final String ID = "AirScooter";

    private static final double SPEED = Config.AIRSCOOTER_SPEED.get();
    /** Reference Interval 100ms, in server ticks. */
    private static final int INTERVAL_TICKS = Config.AIRSCOOTER_INTERVAL_TICKS.get();
    /** Reference Cooldown 500ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.AIRSCOOTER_COOLDOWN_TICKS.get();
    /** Reference Duration 0 (infinite), in server ticks. */
    private static final int DURATION_TICKS = Config.AIRSCOOTER_DURATION_TICKS.get();

    private static final double MAX_HEIGHT = Config.AIRSCOOTER_MAX_HEIGHT.get();
    /** Chime every 3000ms, in server ticks. */
    private static final int CHIME_TICKS = Config.AIRSCOOTER_CHIME_TICKS.get();

    private final ServerLevel level;
    private final long startTick;
    private long lastSpin = 0;
    private long lastChime = 0;
    private long lastMoveCheck;
    private Vec3 lastMovePos = null;
    private double phi = 0;
    private boolean prevMayfly = false;
    private boolean prevFlying = false;
    private boolean started = false;
    private int tick = 0;
    private int floorTick = -10;
    private BlockPos floorCache = null;
    private Slime seat;

    public AirScooter(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.startTick = level.getGameTime();
        this.lastMoveCheck = level.getGameTime();

        AirScooter old = BendingManager.find(player.getUUID(), AirScooter.class);
        if (old != null) {
            BendingManager.remove(old);
            return;
        }
        if (!player.isSprinting()) {
            return;
        }
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (level.getBlockState(eye).isSolidRender(level, eye) || player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        BlockPos underFeet = BlockPos.containing(player.getX(), player.getY() - 0.5, player.getZ());
        if (level.getBlockState(underFeet).isSolidRender(level, underFeet)) {
            return;
        }

        this.prevMayfly = player.getAbilities().mayfly;
        this.prevFlying = player.getAbilities().flying;
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.setSprinting(false);
        if (player.serverLevel().getDifficulty() != Difficulty.PEACEFUL) {
            Slime slime = new Slime(EntityType.SLIME, player.serverLevel());
            slime.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0);
            slime.setSize(1, true);
            slime.setSilent(true);
            slime.setInvulnerable(true);
            slime.setNoAi(true);
            slime.setPersistenceRequired();
            slime.setInvisible(true);
            slime.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            player.serverLevel().addFreshEntity(slime);
            player.startRiding(slime, true);
            this.seat = slime;
        }
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    private BlockPos getFloor(ServerPlayer sp) {
        BlockPos eye = BlockPos.containing(sp.getX(), sp.getEyeY(), sp.getZ());
        for (int i = 0; i <= (int) MAX_HEIGHT; i++) {
            BlockPos p = eye.below(i);
            BlockState s = level.getBlockState(p);
            if (s.isSolidRender(level, p) || !s.getFluidState().isEmpty()) {
                return p.immutable();
            }
        }
        return null;
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
        long gameTime = level.getGameTime();
        if (DURATION_TICKS > 0 && gameTime > this.startTick + DURATION_TICKS) {
            return false;
        }
        this.tick++;
        // Ground column scan cached for 4 ticks; vertical drift within 0.2s is negligible.
        BlockPos floor;
        if (this.tick - this.floorTick >= 4) {
            this.floorTick = this.tick;
            this.floorCache = getFloor(player);
        }
        floor = this.floorCache;
        if (floor == null) {
            return false;
        }
        if (player.isCrouching()
                || (this.seat != null && !this.seat.getPassengers().contains(player))) {
            return false;
        }

        Vec3 velocity = player.getLookAngle().normalize().scale(SPEED);
        if (gameTime > this.startTick + INTERVAL_TICKS) {
            long nowMove = gameTime;
            if (lastMovePos == null) {
                lastMovePos = player.position();
                lastMoveCheck = nowMove;
            } else if (nowMove - lastMoveCheck >= INTERVAL_TICKS) {
                double moved = player.position().distanceTo(lastMovePos);
                double expected = SPEED * 0.3 * (nowMove - lastMoveCheck);
                lastMovePos = player.position();
                lastMoveCheck = nowMove;
                if (moved < expected) {
                    return false;
                }
            }
            spinScooter(player);
        }

        double distance = player.getY() - floor.getY();
        double vy;
        if (distance > 2.75) {
            vy = -0.25;
        } else if (distance < 2.0) {
            vy = 0.25;
        } else {
            vy = 0;
        }
        velocity = new Vec3(velocity.x, vy, velocity.z);

        Vec3 flat = new Vec3(velocity.x, 0, velocity.z);
        if (flat.lengthSqr() > 1.0e-6) {
            flat = flat.normalize().scale(1.2);
        }
        BlockPos ahead = BlockPos.containing(player.getX() + flat.x, floor.getY(), player.getZ() + flat.z);
        BlockState aheadState = level.getBlockState(ahead);
        boolean aheadSolid = aheadState.isSolidRender(level, ahead);
        boolean aheadWater = !aheadState.getFluidState().isEmpty();
        if (!aheadSolid && !aheadWater) {
            velocity = velocity.add(0, -0.1, 0);
        } else {
            BlockPos above = ahead.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.isSolidRender(level, above)
                    || !aboveState.getFluidState().isEmpty()) {
                velocity = velocity.add(0, 0.7, 0);
            }
        }

        BlockPos head2 = BlockPos.containing(player.getX(), player.getY() + 2, player.getZ());
        if (!level.getFluidState(head2).isEmpty()) {
            return true;
        }

        player.setSprinting(false);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        if (this.seat != null) {
            this.seat.move(MoverType.SELF, velocity);
            this.seat.resetFallDistance();
        } else {
            player.setDeltaMovement(velocity);
            player.hurtMarked = true;
        }
        player.resetFallDistance();

        if (gameTime - lastChime > CHIME_TICKS) {
            lastChime = gameTime;
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ELYTRA_FLYING,
                    SoundSource.PLAYERS,
                    0.3F,
                    0.7F);
        }
        return true;
    }

    private void spinScooter(ServerPlayer sp) {
        long now = level.getGameTime();
        if (now - lastSpin < INTERVAL_TICKS) {
            return;
        }
        lastSpin = now;
        this.phi += Math.PI / 10 * 4;
        for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 10) {
            double r = 0.6;
            double x = r * Math.cos(theta) * Math.sin(this.phi);
            double y = r * Math.cos(this.phi);
            double z = r * Math.sin(theta) * Math.sin(this.phi);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSCOOTER_SPIN_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    sp.getX() + x,
                    sp.getY() - 0.2 + y,
                    sp.getZ() + z,
                    Config.AIRSCOOTER_SPIN_PARTICLE_COUNT.get(),
                    0.08,
                    0.08,
                    0.08,
                    0.01);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSCOOTER_SPIN_MIRROR_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    sp.getX() - x,
                    sp.getY() - 0.2 - y,
                    sp.getZ() - z,
                    Config.AIRSCOOTER_SPIN_MIRROR_PARTICLE_COUNT.get(),
                    0.08,
                    0.08,
                    0.08,
                    0.01);
        }
        for (int k = 0; k < 14; k++) {
            double ang = sp.getRandom().nextDouble() * 2 * Math.PI;
            double rr = 0.55 + sp.getRandom().nextDouble() * 0.25;
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSCOOTER_RING_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    sp.getX() + rr * Math.cos(ang),
                    sp.getY() - 0.35 + sp.getRandom().nextDouble() * 0.3,
                    sp.getZ() + rr * Math.sin(ang),
                    Config.AIRSCOOTER_RING_PARTICLE_COUNT.get(),
                    0.03,
                    0.03,
                    0.03,
                    0.005);
        }
    }

    @Override
    public void onRemove() {
        boolean wasStarted = started;
        started = false;
        if (wasStarted) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
            if (player != null) {
                player.stopRiding();
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ELYTRA_FLYING),
                        null));
                player.getAbilities().mayfly = prevMayfly;
                player.getAbilities().flying = prevFlying;
                player.onUpdateAbilities();
                player.resetFallDistance();
            }
            if (seat != null) {
                seat.discard();
                seat = null;
            }
            BendingPlayer bending = BendingPlayer.get(owner);
            if (bending != null) {
                bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
            }
        } else if (seat != null) {
            seat.discard();
            seat = null;
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }
}
