package com.phatabaniyan.avataruniverse.bending;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Bottle rules ported from ProjectKorra's bottle interplay
 * ({@code WaterReturn.hasWaterBottle/emptyWaterBottle}): a water potion
 * anywhere in the inventory can stand in as a one-shot bending source.
 * Re-implemented natively — Bukkit's item API does not exist here.
 * Fill-back (flying water into empty bottles) is deferred.
 */
public final class BendingBottles {
    private BendingBottles() {}

    /** Inventory slot holding a water potion, or -1. */
    public static int firstWaterBottle(ServerPlayer player) {
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.is(Items.POTION)) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents != null && contents.is(Potions.WATER)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean hasWaterBottle(ServerPlayer player) {
        return firstWaterBottle(player) >= 0;
    }

    /**
     * Fill one glass bottle into a water potion (Korra fillBottle return).
     * Returns false when there is no glass bottle.
     */
    public static boolean fillWaterBottle(ServerPlayer player) {
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.is(Items.GLASS_BOTTLE)) {
                ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                if (stack.getCount() == 1) {
                    items.set(i, water);
                } else {
                    stack.shrink(1);
                    if (!player.getInventory().add(water.copy())) {
                        player.drop(water.copy(), false);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Consume one water potion into a glass bottle (Korra emptyWaterBottle).
     * Returns false when there is nothing to consume.
     */
    public static boolean consumeWaterBottle(ServerPlayer player) {
        int slot = firstWaterBottle(player);
        if (slot < 0) {
            return false;
        }
        var items = player.getInventory().items;
        ItemStack stack = items.get(slot);
        if (stack.getCount() == 1) {
            items.set(slot, new ItemStack(Items.GLASS_BOTTLE));
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        return true;
    }
}
