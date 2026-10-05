package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Port of Cozmyc JedCore {@code MetalArmor}: a hidden watcher riding on
 * {@link EarthArmor}. When earth armor forms from a metal source, the
 * dyed leather is swapped for real metal plate (chainmail, or golden for
 * gold-sourced) plus Resistance. Reference values: UseIronArmor false,
 * Resistance Strength 2, Duration 4000ms. Starts itself; never bound.
 */
public class MetalArmor extends EarthAbility {
    public static final String ID = "MetalArmor";

    /** Reference Resistance Duration 4000ms, in server ticks. */
    private static final long RESIST_TICKS = Config.METALARMOR_RESIST_TICKS.get();

    private static final int RESIST_AMPLIFIER = Config.METALARMOR_RESIST_AMPLIFIER.get();

    public MetalArmor(ServerPlayer player) {
        super(player);
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
        EarthArmor armor = BendingManager.find(owner, EarthArmor.class);
        if (armor == null || !armor.isFormed() || !armor.isMetalSourced()) {
            return false;
        }
        boolean gold = armor.isGoldSourced();
        var inv = player.getInventory().armor;
        inv.set(3, new ItemStack(gold ? Items.GOLDEN_HELMET : Items.CHAINMAIL_HELMET));
        inv.set(2, new ItemStack(gold ? Items.GOLDEN_CHESTPLATE : Items.CHAINMAIL_CHESTPLATE));
        inv.set(1, new ItemStack(gold ? Items.GOLDEN_LEGGINGS : Items.CHAINMAIL_LEGGINGS));
        inv.set(0, new ItemStack(gold ? Items.GOLDEN_BOOTS : Items.CHAINMAIL_BOOTS));
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, (int) RESIST_TICKS, RESIST_AMPLIFIER, false, false, true));
        return false;
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
}
