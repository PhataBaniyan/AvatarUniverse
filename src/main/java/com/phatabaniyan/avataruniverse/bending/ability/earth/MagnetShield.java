package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of Cozmyc JedCore {@code MagnetShield}: sneak to raise a magnetic
 * field that shoves iron and gold away - loose items, falling blocks,
 * incoming arrows (reversed mid-air!) and anything wearing or holding
 * metal. Click instead for a 6s untended shield. Five expanding particle
 * rings show the field. Reference values: Duration 6000ms, Shift/Click
 * Cooldowns 5000ms, Range 5, RepelArrows on, RepelLivingEntities on,
 * Velocity 0.1. Reference gates on metalbending; here any earthbender may
 * use it.
 */
public class MagnetShield extends EarthAbility {
    public static final String ID = "MagnetShield";

    /** Reference Duration 6000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.MAGNETSHIELD_DURATION_TICKS.get();
    /** Reference Shift/Click Cooldowns 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.MAGNETSHIELD_COOLDOWN_TICKS.get();

    private static final double RANGE = Config.MAGNETSHIELD_RANGE.get();
    private static final double VELOCITY = Config.MAGNETSHIELD_VELOCITY.get();

    private final boolean clickMode;
    private final long bornTick;
    private int angle;

    public MagnetShield(ServerPlayer player, boolean clickMode) {
        super(player);
        this.clickMode = clickMode;
        this.bornTick = player.level().getGameTime();
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.CONDUIT_ACTIVATE,
                SoundSource.PLAYERS,
                1.0F,
                1.5F);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (clickMode) {
            if (level.getGameTime() - bornTick >= DURATION_TICKS) {
                cool(owner, player, ID, COOLDOWN_TICKS);
                return false;
            }
        } else if (!player.isShiftKeyDown()) {
            cool(owner, player, ID, COOLDOWN_TICKS);
            return false;
        }
        fieldLines();
        repel();
        return true;
    }

    /** Five expanding rings of magnetic dust around the caster. */
    private void fieldLines() {
        angle = (angle + 8) % 360;
        DustParticleOptions magnetic = new DustParticleOptions(new Vector3f(0.85F, 0.85F, 0.9F), 1.0F);
        Vec3 base = player.position();
        for (int loop = 0; loop < 5; loop++) {
            double radius = RANGE * (1 - (double) loop / 5);
            double height = (loop - 2.5) * 0.06 + 0.3;
            for (int i = 0; i < 30; i++) {
                double theta = Math.toRadians(angle) + 2 * Math.PI * i / 30;
                level.sendParticles(
                        magnetic,
                        base.x + Math.cos(theta) * radius,
                        base.y + height,
                        base.z + Math.sin(theta) * radius,
                        1,
                        0,
                        0,
                        0,
                        0);
            }
        }
    }

    /** Shove metal items and blocks away, reverse arrows, push metal-clad livings. */
    private void repel() {
        Vec3 center = player.position();
        for (Entity entity : level.getEntitiesOfClass(
                Entity.class,
                new AABB(center.subtract(RANGE, RANGE, RANGE), center.add(RANGE, RANGE, RANGE)),
                Entity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (entity.position().distanceToSqr(center) > RANGE * RANGE) {
                continue;
            }
            if (entity instanceof ItemEntity item && isMetal(item.getItem())) {
                Vec3 push = item.position().subtract(center).normalize().scale(VELOCITY);
                item.setDeltaMovement(push);
            } else if (entity instanceof FallingBlockEntity fb && isMetal(fb.getBlockState())) {
                Vec3 push = fb.position().subtract(center).normalize().scale(VELOCITY);
                fb.setDeltaMovement(push);
                fb.dropItem = false;
            } else if (entity instanceof AbstractArrow arrow) {
                Vec3 flight = arrow.getDeltaMovement();
                arrow.setDeltaMovement(flight.scale(-1));
            } else if (entity instanceof LivingEntity living && wearsMetal(living)) {
                Vec3 push = living.position().subtract(center).normalize().scale(VELOCITY);
                living.setDeltaMovement(push);
                living.hurtMarked = true;
            }
        }
    }

    /** Reference Materials list: iron and gold families plus clocks, compasses, shields, anvils. */
    private static boolean isMetal(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String key = stack.getItem().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        return key.contains("IRON") || key.contains("GOLD") || isInstrument(key);
    }

    private static boolean isMetal(BlockState state) {
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        return key.contains("IRON") || key.contains("GOLD") || isInstrument(key);
    }

    private static boolean isInstrument(String key) {
        return key.contains("CLOCK") || key.contains("COMPASS") || key.contains("SHIELD") || key.contains("ANVIL");
    }

    private static boolean wearsMetal(LivingEntity living) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            if (isMetal(living.getItemBySlot(slot))) {
                return true;
            }
        }
        return isMetal(living.getMainHandItem()) || isMetal(living.getOffhandItem());
    }

    @Override
    public void onRemove() {}

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
