package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code ShrapnelShot} + {@code ShrapnelBlast}
 * (addons/.../ability/earth/ShrapnelShot.java, ShrapnelBlast.java): click
 * to throw a nugget like shrapnel, sneak-click for a 9-shell shotgun cone.
 * Iron nuggets preferred, gold accepted, one per shell. Shells fly real
 * ballistics with crit trails, hitting for up to 2 scaled by retained
 * speed with multi-hit pacing; dead shells vanish. Reference values: Shot
 * Cooldown 2000ms, Shot Damage 2, Shot Speed 2.3, Blast Cooldown 8000ms,
 * Blast Shots 9, Spread 24, Blast Speed 1.7. Reference gates on
 * metalbending; here any earthbender may use it.
 */
public class Shrapnel extends EarthAbility {
    public static final String ID = "Shrapnel";

    /** Reference Shot Cooldown 2000ms, in server ticks. */
    private static final long SHOT_COOLDOWN = Config.msToTicks(Config.SHRAPNEL_SHOT_COOLDOWN_MS.get());
    /** Reference Blast Cooldown 8000ms, in server ticks. */
    private static final long BLAST_COOLDOWN = Config.msToTicks(Config.SHRAPNEL_BLAST_COOLDOWN_MS.get());

    private static final float DAMAGE = Config.SHRAPNEL_DAMAGE.get().floatValue();
    private static final double SHOT_SPEED = Config.SHRAPNEL_SHOT_SPEED.get();
    private static final double BLAST_SPEED = Config.SHRAPNEL_BLAST_SPEED.get();
    private static final int BLAST_SHOTS = Config.SHRAPNEL_BLAST_SHOTS.get();
    private static final double BLAST_SPREAD = Math.toRadians(Config.SHRAPNEL_BLAST_SPREAD_DEGREES.get());

    private final List<UUID> shells = new ArrayList<>();
    private final double launchSpeed;
    private final boolean valid;

    public Shrapnel(ServerPlayer player, boolean blast) {
        super(player);
        if (blast) {
            int fired = 0;
            for (int i = 0; i < BLAST_SHOTS; i++) {
                Vec3 dir = player.getLookAngle().normalize();
                double yaw = (level.random.nextDouble() - 0.5) * 2 * BLAST_SPREAD;
                double pitch = (level.random.nextDouble() - 0.5) * 2 * BLAST_SPREAD;
                double cos = Math.cos(yaw);
                double sin = Math.sin(yaw);
                Vec3 fanned = new Vec3(dir.x * cos - dir.z * sin, dir.y + pitch, dir.x * sin + dir.z * cos).normalize();
                if (fire(fanned, BLAST_SPEED)) {
                    fired++;
                } else {
                    break;
                }
            }
            valid = fired > 0;
            if (valid) {
                cool(owner, player, ID, BLAST_COOLDOWN);
            }
        } else {
            Vec3 dir = player.getLookAngle().normalize();
            valid = fire(dir, SHOT_SPEED);
            if (valid) {
                cool(owner, player, ID, SHOT_COOLDOWN);
            }
        }
        this.launchSpeed = blast ? BLAST_SPEED : SHOT_SPEED;
    }

    @Override
    public String name() {
        return ID;
    }

    /** Consume one nugget and throw it down a direction. */
    private boolean fire(Vec3 direction, double speed) {
        ItemStack ammo = null;
        int slot = -1;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.IRON_NUGGET) || stack.is(Items.GOLD_NUGGET)) {
                boolean iron = stack.is(Items.IRON_NUGGET);
                if (ammo == null || (iron && !ammo.is(Items.IRON_NUGGET))) {
                    ammo = stack;
                    slot = i;
                    if (iron) {
                        break;
                    }
                }
            }
        }
        if (ammo == null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Need iron or gold nuggets."), true);
            return false;
        }
        ItemStack thrown = ammo.copyWithCount(1);
        ammo.shrink(1);
        if (ammo.isEmpty()) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        Vec3 eye = player.getEyePosition();
        ItemEntity shell = new ItemEntity(level, eye.x, eye.y - 0.2, eye.z, thrown);
        shell.setDefaultPickUpDelay();
        shell.setDeltaMovement(direction.add(0, 0.105, 0).normalize().scale(speed));
        level.addFreshEntity(shell);
        shells.add(shell.getUUID());
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.6F, 1.0F);
        return true;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        for (UUID id : new ArrayList<>(shells)) {
            if (!(level.getEntity(id) instanceof ItemEntity shell) || !shell.isAlive()) {
                shells.remove(id);
                continue;
            }
            if (shell.onGround()) {
                shell.discard();
                shells.remove(id);
                continue;
            }
            Vec3 at = shell.position();
            level.sendParticles(
                    BendingTheme.particle(Config.SHRAPNEL_TRAIL_PARTICLE.get(), ParticleTypes.CRIT),
                    at.x,
                    at.y + 0.3,
                    at.z,
                    Config.SHRAPNEL_TRAIL_PARTICLE_COUNT.get(),
                    0,
                    0,
                    0,
                    0);
            if (level.getGameTime() % 4 == 0) {
                level.playSound(null, at.x, at.y, at.z, SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.2F, 1.0F);
            }
            double dmg = Math.max(
                    0, Math.min(DAMAGE, DAMAGE * (shell.getDeltaMovement().length() / launchSpeed)));
            boolean spent = false;
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(at.subtract(1, 1, 1), at.add(1, 1, 1)).inflate(0.5),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                if (entity.position().distanceToSqr(at) > 2.25
                        && entity.getEyePosition().distanceToSqr(at) > 2.25) {
                    continue;
                }
                level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.4F, 1.0F);
                entity.hurt(player.damageSources().playerAttack(player), (float) dmg);
                entity.invulnerableTime = 0;
                spent = true;
                break;
            }
            if (spent) {
                shell.discard();
                shells.remove(id);
            }
        }
        return !shells.isEmpty();
    }

    @Override
    public void onRemove() {
        for (UUID id : new ArrayList<>(shells)) {
            if (level.getEntity(id) instanceof ItemEntity shell) {
                shell.discard();
            }
        }
        shells.clear();
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
