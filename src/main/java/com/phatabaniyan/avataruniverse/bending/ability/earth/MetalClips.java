package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code MetalClips}
 * (core/.../earthbending/metal/MetalClips.java from the local
 * ProjectKorra-master copy). Click with iron ingots in the pocket to hurl
 * one at whatever you are looking at (speed 1.2, range 10): plain hits
 * deal 2 damage, while players/zombies/skeletons get wrapped piece by
 * piece (chest, boots, legs, helmet, 10s) with plain clicks. Hold sneak
 * at a wrapped victim to pin them bloodbending-style at your gaze,
 * release to throw them, sneak-click to crush for 1. Sneak with empty
 * hands magnets
 * loose iron to you from 20 blocks. No cooldowns anywhere by request;
 * ingots and the armor timer are the only limits. Reference gates the 4th
 * clip and loot behind permissions; here 4 clips always work and loot is
 * skipped. Reference gates on metalbending; here any earthbender may use
 * it.
 */
public class MetalClips extends EarthAbility {
    public static final String ID = "MetalClips";

    private static final double RANGE = Config.METALCLIPS_RANGE.get();
    /** Fully-stretched bow speed: flat, fast, accurate. */
    private static final double SHOOT_SPEED = Config.METALCLIPS_SHOOT_SPEED.get();

    private static final float HIT_DAMAGE = Config.METALCLIPS_HIT_DAMAGE.get().floatValue();
    private static final float CRUSH_DAMAGE =
            Config.METALCLIPS_CRUSH_DAMAGE.get().floatValue();
    private static final long ARMOR_TICKS = Config.METALCLIPS_ARMOR_TICKS.get();
    private static final double MAGNET_RANGE = Config.METALCLIPS_MAGNET_RANGE.get();
    private static final double MAGNET_SPEED = Config.METALCLIPS_MAGNET_SPEED.get();
    private static final long SHOT_LIFE_TICKS = Config.METALCLIPS_SHOT_LIFE_TICKS.get();
    private static final int MAX_CLIPS = Config.METALCLIPS_MAX_CLIPS.get();

    private LivingEntity victim;
    private int clips;
    private long armorBorn;
    private boolean hasSnuck;
    private boolean magnetizing;
    private final List<UUID> shots = new ArrayList<>();
    private final Map<UUID, Long> shotBorn = new HashMap<>();
    private final Map<EquipmentSlot, ItemStack> savedArmor = new EnumMap<>(EquipmentSlot.class);
    private boolean armorSaved;

    public MetalClips(ServerPlayer player, boolean sneakStarted) {
        super(player);
        this.magnetizing = sneakStarted;
        if (!sneakStarted) {
            // First click throws immediately: a silent start with no shot
            // is a dead click and reads as broken.
            shoot();
        }
    }

    @Override
    public String name() {
        return ID;
    }

    /**
     * Click path: sneaking at the controlled victim crushes it, otherwise
     * throw another ingot (stacking to full armor requires plain clicks).
     */
    public void click() {
        if (victim != null && victim.isAlive() && player.isShiftKeyDown()) {
            victim.hurt(player.damageSources().playerAttack(player), CRUSH_DAMAGE);
            clank(victim.position());
            return;
        }
        shoot();
    }

    /** Hurl one ingot from the pocket down the gaze. */
    public void shoot() {
        if (!takeIngot()) {
            feedback("Need an iron ingot.");
            return;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity target = eyeVictim(RANGE, 2.0);
        // Aim chest, not feet: feet-aimed arcs die in the dirt short of
        // the victim and the clips never form.
        Vec3 aim = target != null
                ? target.position().add(0, 0.8, 0).subtract(eye).normalize()
                : eyeTargetPoint(RANGE).subtract(eye).normalize();
        ItemEntity shot = new ItemEntity(level, eye.x, eye.y - 0.2, eye.z, new ItemStack(Items.IRON_INGOT));
        shot.setDeltaMovement(aim.scale(SHOOT_SPEED));
        level.addFreshEntity(shot);
        shots.add(shot.getUUID());
        shotBorn.put(shot.getUUID(), level.getGameTime());
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    /** Ammo comes from the main inventory only: never strips worn armor or offhand. */
    private boolean takeIngot() {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.IRON_INGOT)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private Vec3 eyeTargetPoint(double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        BlockPos hit = eyeTarget(range);
        return hit != null ? Vec3.atCenterOf(hit) : eye.add(look.scale(range));
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (victim != null && (!victim.isAlive() || victim.isRemoved() || victim.level() != level)) {
            releaseVictim(true);
            victim = null;
            clips = 0;
        }
        if (player.isShiftKeyDown()) {
            hasSnuck = true;
        }
        if (!player.isShiftKeyDown()) {
            if (magnetizing) {
                return false;
            }
            magnetizing = false;
            if (clips > 0 && hasSnuck && victim != null) {
                throwVictim();
                return false;
            }
        } else if (victim != null) {
            magnetizing = false;
            steerVictim();
        } else {
            magnetizing = true;
            magnetize();
        }
        if (victim != null && level.getGameTime() - armorBorn > ARMOR_TICKS) {
            return false;
        }
        flyShots();
        return true;
    }

    /** Sneak-steer: 1 clip drags to you, 2 toward gaze, 3+ full puppet. */
    private void steerVictim() {
        Vec3 gaze =
                player.getEyePosition().add(player.getLookAngle().normalize().scale(10));
        Vec3 at = victim.position();
        Vec3 move = null;
        if (clips <= 1) {
            if (at.distanceTo(player.position()) > 1.5) {
                move = player.position().subtract(at).normalize().scale(0.25);
            }
        } else if (clips == 2) {
            if (at.distanceTo(gaze) > 1.2) {
                move = gaze.subtract(at).normalize().scale(0.3);
            }
        } else {
            if (at.distanceTo(gaze) > 1.2) {
                move = gaze.subtract(at).normalize().scale(0.55);
            } else {
                victim.setDeltaMovement(Vec3.ZERO);
            }
            victim.resetFallDistance();
        }
        if (move != null) {
            victim.setDeltaMovement(move);
            victim.hurtMarked = true;
            victim.resetFallDistance();
        }
        if (victim instanceof ServerPlayer target) {
            target.displayClientMessage(Component.literal("* MetalClipped *"), true);
        }
    }

    /** Release sneak: hurl the victim onward, armor melts into ingots. */
    private void throwVictim() {
        Vec3 push = victim.position().subtract(player.position()).normalize().scale(clips / 2.0);
        victim.setDeltaMovement(push);
        victim.hurtMarked = true;
        BendingManager.remove(this);
    }

    /** Sneak magnet: drag loose iron to the caster. */
    private void magnetize() {
        Vec3 center = player.position();
        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(center.subtract(1, 1, 1), center.add(1, 1, 1)).inflate(MAGNET_RANGE - 1.0),
                e -> e.isAlive() && isMetal(e.getItem()) && !shots.contains(e.getUUID()))) {
            Vec3 pull = center.subtract(item.position())
                    .normalize()
                    .scale(MAGNET_SPEED)
                    .add(0, 0.2, 0);
            item.setDeltaMovement(pull);
        }
    }

    private static boolean isMetal(ItemStack stack) {
        return stack.is(Items.IRON_INGOT)
                || stack.is(Items.IRON_BLOCK)
                || stack.is(Items.IRON_HELMET)
                || stack.is(Items.IRON_CHESTPLATE)
                || stack.is(Items.IRON_LEGGINGS)
                || stack.is(Items.IRON_BOOTS)
                || stack.is(Items.IRON_AXE)
                || stack.is(Items.IRON_PICKAXE)
                || stack.is(Items.IRON_SWORD)
                || stack.is(Items.IRON_HOE)
                || stack.is(Items.IRON_SHOVEL);
    }

    /** Nearest clip-compatible victim (player/zombie/skeleton) to a point. */
    private LivingEntity armorableNear(Vec3 at, double radius) {
        LivingEntity best = null;
        double bestDist = radius * radius;
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(at.subtract(radius, radius, radius), at.add(radius, radius, radius)),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (!(entity instanceof Player
                    || entity.getType().getDescriptionId().contains("zombie")
                    || entity.getType().getDescriptionId().contains("skeleton"))) {
                continue;
            }
            double d = Math.min(
                    entity.position().distanceToSqr(at), entity.getEyePosition().distanceToSqr(at));
            if (d < bestDist) {
                bestDist = d;
                best = entity;
            }
        }
        return best;
    }

    /** Fling ingots; first touch on armorables wraps, else damage + drop. */
    private void flyShots() {
        for (UUID id : new ArrayList<>(shots)) {
            if (!(level.getEntity(id) instanceof ItemEntity shot) || !shot.isAlive()) {
                shots.remove(id);
                shotBorn.remove(id);
                continue;
            }
            if (shot.onGround()) {
                // Arced short but landed at their feet: still clips.
                LivingEntity grounded = armorableNear(shot.position(), 2.5);
                if (grounded != null) {
                    attach(grounded);
                    shot.discard();
                }
                shots.remove(id);
                shotBorn.remove(id);
                continue;
            }
            Vec3 at = shot.position();
            boolean spent = false;
            LivingEntity caught = armorableNear(at, 2.5);
            if (caught != null) {
                attach(caught);
                shot.discard();
                shots.remove(id);
                shotBorn.remove(id);
                continue;
            }
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(at.subtract(1, 1, 1), at.add(1, 1, 1)).inflate(0.8),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                if (entity.position().distanceToSqr(at) > 3.24
                        && entity.getEyePosition().distanceToSqr(at) > 3.24) {
                    continue;
                }
                entity.hurt(player.damageSources().playerAttack(player), HIT_DAMAGE);
                dropIngots(entity.position(), 1);
                shot.discard();
                shots.remove(id);
                shotBorn.remove(id);
                spent = true;
                break;
            }
            if (!spent && level.getGameTime() - shotBorn.getOrDefault(id, 0L) > SHOT_LIFE_TICKS) {
                shots.remove(id);
                shotBorn.remove(id);
            }
        }
    }

    /** Wrap one more clip, switching victims cleanly like the reference. */
    private void attach(LivingEntity entity) {
        if (victim != null && !victim.getUUID().equals(entity.getUUID())) {
            releaseVictim(true);
            victim = null;
            clips = 0;
        }
        if (clips >= MAX_CLIPS) {
            return;
        }
        victim = entity;
        clips = Math.min(MAX_CLIPS, clips + 1);
        dress();
        armorBorn = level.getGameTime();
        clank(entity.position());
    }

    /** Iron pieces per clip count; originals restored on release. */
    private void dress() {
        if (victim == null) {
            return;
        }
        if (!armorSaved) {
            armorSaved = true;
            if (victim instanceof ServerPlayer target) {
                savedArmor.put(
                        EquipmentSlot.FEET, target.getInventory().armor.get(0).copy());
                savedArmor.put(
                        EquipmentSlot.LEGS, target.getInventory().armor.get(1).copy());
                savedArmor.put(
                        EquipmentSlot.CHEST, target.getInventory().armor.get(2).copy());
                savedArmor.put(
                        EquipmentSlot.HEAD, target.getInventory().armor.get(3).copy());
            } else {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                        savedArmor.put(slot, victim.getItemBySlot(slot).copy());
                    }
                }
            }
        }
        setPiece(EquipmentSlot.CHEST, 2, clips >= 1 ? new ItemStack(Items.IRON_CHESTPLATE) : null);
        setPiece(EquipmentSlot.FEET, 0, clips >= 2 ? new ItemStack(Items.IRON_BOOTS) : null);
        setPiece(EquipmentSlot.LEGS, 1, clips >= 3 ? new ItemStack(Items.IRON_LEGGINGS) : null);
        setPiece(EquipmentSlot.HEAD, 3, clips >= 4 ? new ItemStack(Items.IRON_HELMET) : null);
    }

    private void setPiece(EquipmentSlot slot, int playerIdx, ItemStack piece) {
        if (piece == null) {
            return;
        }
        if (victim instanceof ServerPlayer target) {
            target.getInventory().armor.set(playerIdx, piece);
        } else {
            victim.setItemSlot(slot, piece);
        }
    }

    /** Strip the clips, hand back originals, drop the ingots. */
    private void releaseVictim(boolean drop) {
        if (victim == null) {
            return;
        }
        if (armorSaved) {
            if (victim instanceof ServerPlayer target) {
                target.getInventory().armor.set(0, savedArmor.getOrDefault(EquipmentSlot.FEET, ItemStack.EMPTY));
                target.getInventory().armor.set(1, savedArmor.getOrDefault(EquipmentSlot.LEGS, ItemStack.EMPTY));
                target.getInventory().armor.set(2, savedArmor.getOrDefault(EquipmentSlot.CHEST, ItemStack.EMPTY));
                target.getInventory().armor.set(3, savedArmor.getOrDefault(EquipmentSlot.HEAD, ItemStack.EMPTY));
            } else if (victim.isAlive()) {
                for (Map.Entry<EquipmentSlot, ItemStack> entry : savedArmor.entrySet()) {
                    victim.setItemSlot(entry.getKey(), entry.getValue());
                }
            }
            armorSaved = false;
            savedArmor.clear();
        }
        if (drop && clips > 0 && victim.isAlive()) {
            dropIngots(victim.position(), clips);
        }
        victim = null;
        clips = 0;
    }

    private void dropIngots(Vec3 at, int amount) {
        ItemEntity item = new ItemEntity(level, at.x, at.y + 0.5, at.z, new ItemStack(Items.IRON_INGOT, amount));
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    private void clank(Vec3 at) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5F, 1.4F);
    }

    private void feedback(String text) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            player.displayClientMessage(Component.literal(text), true);
        }
    }

    @Override
    public void onRemove() {
        for (UUID id : new ArrayList<>(shots)) {
            if (level.getEntity(id) instanceof ItemEntity shot) {
                shot.discard();
            }
        }
        shots.clear();
        shotBorn.clear();
        releaseVictim(true);
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }
}
