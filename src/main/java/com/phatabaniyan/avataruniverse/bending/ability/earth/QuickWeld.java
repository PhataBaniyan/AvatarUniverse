package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Port of ProjectAddons {@code QuickWeld}
 * (addons/.../ability/earth/QuickWeld.java): sneak with a damaged iron
 * tool, weapon or armor piece in the main hand to weld it back together.
 * Every 1.25s one iron ingot from the pocket is consumed for +25
 * durability with an anvil clink. Stops when you stand up, run out of
 * ingots, or the piece is whole. Reference values: Cooldown 1000ms,
 * RepairAmount 25, RepairInterval 1250ms. Reference gates on
 * metalbending; here any earthbender may use it.
 */
public class QuickWeld extends EarthAbility {
    public static final String ID = "QuickWeld";

    /** Reference Cooldown 1000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.QUICKWELD_COOLDOWN_MS.get());

    private static final int REPAIR_AMOUNT = Config.QUICKWELD_REPAIR_AMOUNT.get();
    /** Reference RepairInterval 1250ms, in server ticks. */
    private static final long REPAIR_EVERY = Config.msToTicks(Config.QUICKWELD_REPAIR_EVERY_MS.get());

    private long lastRepair = -100L;

    public QuickWeld(ServerPlayer player) {
        super(player);
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: a damaged iron piece in the main hand plus an ingot to burn. */
    public static boolean canBegin(ServerPlayer player) {
        return isWeldable(player.getMainHandItem()) && hasIngot(player);
    }

    private static boolean isWeldable(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamaged()) {
            return false;
        }
        return stack.is(Items.IRON_AXE)
                || stack.is(Items.IRON_BOOTS)
                || stack.is(Items.IRON_CHESTPLATE)
                || stack.is(Items.IRON_HELMET)
                || stack.is(Items.IRON_HOE)
                || stack.is(Items.IRON_LEGGINGS)
                || stack.is(Items.IRON_PICKAXE)
                || stack.is(Items.IRON_SHOVEL)
                || stack.is(Items.IRON_SWORD);
    }

    private static boolean hasIngot(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().getItem(i).is(Items.IRON_INGOT)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (!isWeldable(held)) {
            return false;
        }
        long now = level.getGameTime();
        if (now - lastRepair < REPAIR_EVERY) {
            return true;
        }
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.IRON_INGOT)) {
                stack.shrink(1);
                held.setDamageValue(Math.max(0, held.getDamageValue() - REPAIR_AMOUNT));
                lastRepair = now;
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ANVIL_USE,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.0F);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRemove() {
        cool(owner, player, ID, COOLDOWN_TICKS);
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
