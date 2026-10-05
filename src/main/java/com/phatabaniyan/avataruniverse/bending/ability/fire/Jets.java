package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code Jets}: click to ignite — hover on slow
 * flight or glide down the look vector. Activating while moving fast
 * enough and looking at your motion enters flight mode automatically.
 * Reference values: Cooldown 4000-12000ms scaled by use, Duration 20000ms,
 * FlySpeed 0.65, HoverSpeed 0.065, SpeedThreshold 2.4, DamageThreshold 4,
 * MaxHeight -1.
 */
public class Jets extends BendingAbility {
    public static final String ID = "Jets";

    private final ServerLevel level;
    private final ServerPlayer player;
    private final long startedTick;
    private final long durationTicks;
    private final long minCooldownTicks;
    private final long maxCooldownTicks;
    private final double flySpeed;
    private double hoverSpeed;
    private final double speedThreshold;
    private final double damageThreshold;
    private final int maxHeight;
    private final float prevFlySpeed;
    private boolean hovering;
    private boolean gliding;
    private boolean started;

    public Jets(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.startedTick = level.getGameTime();
        this.durationTicks = (long) (Config.JETS_DURATION_MS.get() / 50);
        this.minCooldownTicks = (long) (Config.JETS_COOLDOWN_MIN_MS.get() / 50);
        this.maxCooldownTicks = (long) (Config.JETS_COOLDOWN_MAX_MS.get() / 50);
        this.flySpeed = Config.JETS_FLY_SPEED.get();
        this.hoverSpeed = Config.JETS_HOVER_SPEED.get();
        this.speedThreshold = Config.JETS_SPEED_THRESHOLD.get();
        this.damageThreshold = player.getHealth() - Config.JETS_DAMAGE_THRESHOLD.get();
        this.maxHeight = Config.JETS_MAX_HEIGHT.get();
        this.prevFlySpeed = player.getAbilities().getFlyingSpeed();

        if (player.onGround() || player.isEyeInFluid(FluidTags.WATER) || player.isEyeInFluid(FluidTags.LAVA)) {
            return;
        }
        Vec3 velocity = player.getDeltaMovement();
        Vec3 look = player.getLookAngle().normalize();
        if (velocity.length() > speedThreshold && angleTo(velocity, look) < 30.0) {
            this.gliding = true;
            this.hovering = false;
        } else {
            this.gliding = false;
            this.hovering = true;
        }
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        player.getAbilities().setFlyingSpeed((float) hoverSpeed);
        this.started = true;
    }

    private static double angleTo(Vec3 a, Vec3 b) {
        double cos = a.dot(b) / (a.length() * b.length());
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, cos))));
    }

    @Override
    public String name() {
        return ID;
    }

    /** Gate: firebender, airborne, dry — the constructor applies it too. */
    public static boolean canBegin(ServerPlayer player) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        return bending != null
                && bending.hasElement(BendingElement.FIRE)
                && bending.isToggled()
                && !player.onGround()
                && !player.isInWater()
                && !player.isEyeInFluid(FluidTags.LAVA);
    }

    /** Click while live: sneak ends it, else hover pops between glide/hover. */
    public void clickFunction() {
        if (player.isShiftKeyDown()) {
            BendingManager.remove(this);
            return;
        }
        if (hovering) {
            hovering = false;
            gliding = true;
        } else {
            hovering = true;
            gliding = false;
        }
    }

    private boolean canLiftHere() {
        if (maxHeight <= 0) {
            return true;
        }
        BlockPos feet = player.blockPosition();
        for (int i = 1; i <= maxHeight; i++) {
            BlockPos below = feet.below(i);
            if (!level.getBlockState(below).isAir()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean progress() {
        if (!started || player.isRemoved() || player.isDeadOrDying() || player.level() != level) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null || !bending.hasElement(BendingElement.FIRE) || !bending.isToggled()) {
            return false;
        }
        if (player.onGround()) {
            return false;
        }
        if (!level.getFluidState(player.blockPosition()).isEmpty()) {
            return false;
        }
        long elapsed = level.getGameTime() - startedTick;
        if (durationTicks > 0 && elapsed > durationTicks) {
            return false;
        }
        if (player.getHealth() < damageThreshold) {
            return false;
        }
        if (!canLiftHere()) {
            // Ceiling reached: cut the jets, but do not end outright.
            player.getAbilities().flying = false;
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
            return true;
        }
        Vec3 pDirection;
        if (hovering) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, -0.015, 0.0));
            pDirection = new Vec3(0.0, -0.4, 0.0);
        } else if (gliding) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            if (!player.isFallFlying()) {
                player.startFallFlying();
            }
            Vec3 look = player.getLookAngle().normalize();
            Vec3 velocity = look.scale(flySpeed);
            if (player.getDeltaMovement().y < 0) {
                velocity = velocity.add(player.getDeltaMovement().scale(0.2));
            }
            player.setDeltaMovement(velocity);
            player.hurtMarked = true;
            pDirection = look.scale(-0.4);
        } else {
            return false;
        }
        player.resetFallDistance();
        for (int i = 0; i < 4; i++) {
            Vec3 p = player.position().add(pDirection.scale(i));
            level.sendParticles(
                    BendingTheme.flame(owner), p.x, p.y + 0.05, p.z, 4 - i, 0.3 - i / 10.0, 0.04, 0.3 - i / 10.0, 0.0);
        }
        if (level.getGameTime() % 3 == 0) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.2F);
        }
        return true;
    }

    @Override
    public void onRemove() {
        if (!started) {
            return;
        }
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlyingSpeed(prevFlySpeed);
        player.onUpdateAbilities();
        if (player.isFallFlying()) {
            player.stopFallFlying();
        }
        player.resetFallDistance();
        // Cooldown scales with time spent in the air, like the reference.
        long elapsed = Math.max(0L, level.getGameTime() - startedTick);
        double pct = durationTicks > 0 ? Math.min(1.0, elapsed / (double) durationTicks) : 0.0;
        long cooldown = minCooldownTicks + (long) ((maxCooldownTicks - minCooldownTicks) * pct);
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + cooldown);
        }
    }
}
