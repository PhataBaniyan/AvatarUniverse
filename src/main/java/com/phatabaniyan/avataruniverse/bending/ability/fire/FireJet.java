package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireJet}: sneak to ride a jet of flame along
 * the look direction, thrust decaying to half over the flight. A fire block
 * is left at the launch feet and cleared on landing, flight is granted for
 * the ride and restored after, and a short fire-resistance covers ignition.
 * Retoggling mid-jet cancels the old ride. Reference values: Duration
 * 2000ms (40 ticks), Speed 0.8, Cooldown 7000ms (140 ticks), gliding overlay
 * on.
 */
public class FireJet extends BendingAbility {
    public static final String ID = "FireJet";

    /** Reference Duration 2000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.FIREJET_DURATION_MS.get());
    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIREJET_COOLDOWN_MS.get());

    private static final double SPEED = Config.FIREJET_SPEED.get();
    private static final boolean SHOW_GLIDING = true;

    private final ServerLevel level;
    private long startTick;
    private long durationTicks = DURATION_TICKS;
    private double speed = SPEED;
    private boolean started = false;
    private boolean prevMayfly;
    private boolean prevFlying;
    private boolean prevGliding;
    private BlockPos firePos;

    public FireJet(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        FireJet old = BendingManager.find(player.getUUID(), FireJet.class);
        if (old != null) {
            BendingManager.remove(old);
            return;
        }

        BlockPos feet = player.blockPosition();
        BlockState feetState = player.level().getBlockState(feet);
        boolean canPlaceFire = player.level().getBlockState(feet.below()).isSolid();
        if (!(feetState.isAir() || feetState.is(Blocks.FIRE) || canPlaceFire)) {
            return;
        }

        Vec3 look = player.getLookAngle().normalize().scale(this.speed);
        player.setDeltaMovement(look);
        player.hurtMarked = true;

        if (feetState.isAir()) {
            player.level().setBlockAndUpdate(feet, Blocks.FIRE.defaultBlockState());
            this.firePos = feet.immutable();
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, Config.msToTicks(Config.FIREJET_RESIST_MS.get()), 0, false, false));

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
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS,
                    0.7F,
                    1.2F);
            level.sendParticles(
                    BendingTheme.particle(Config.FIREJET_EXTINGUISH_PARTICLE.get(), ParticleTypes.CLOUD),
                    player.getX(),
                    player.getY() + 0.5,
                    player.getZ(),
                    Config.FIREJET_EXTINGUISH_PARTICLE_COUNT.get(),
                    0.4,
                    0.4,
                    0.4,
                    0.05);
            return false;
        }
        long gameTime = level.getGameTime();
        if (gameTime > this.startTick + this.durationTicks) {
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
                BendingTheme.particle(Config.FIREJET_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                player.getX() + side.x,
                baseY,
                player.getZ() + side.z,
                Config.FIREJET_TRAIL_PARTICLE_COUNT.get(),
                0.12,
                0.12,
                0.12,
                0.01);
        level.sendParticles(
                BendingTheme.particle(Config.FIREJET_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                player.getX() - side.x,
                baseY,
                player.getZ() - side.z,
                Config.FIREJET_TRAIL_PARTICLE_COUNT.get(),
                0.12,
                0.12,
                0.12,
                0.01);

        double elapsed = gameTime - this.startTick;
        double timefactor = 1.0 - elapsed / (2.0 * this.durationTicks);
        Vec3 velocity = look.scale(this.speed * timefactor);
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
        if (firePos != null && level.getBlockState(firePos).is(Blocks.FIRE)) {
            level.setBlockAndUpdate(firePos, Blocks.AIR.defaultBlockState());
        }
        firePos = null;
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    private static boolean gate(java.util.UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }
}
