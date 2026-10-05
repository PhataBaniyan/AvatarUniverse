package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.Config;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Time mastery: attuned online ticks slowly unlock each base element's
 * sub-elements in canon order, one per {@code masteryAttunementMs} (default
 * ten Minecraft days). Avatars are never granted here — operators hand those
 * out through {@code /au add}. Skips untoggled and logged-out benders; all
 * progress persists in the vanilla player tag.
 */
public final class BendingMastery {
    private BendingMastery() {}

    /** Unlock order per base element (Avatar deliberately absent). */
    private static final Map<BendingElement, List<BendingElement>> ORDER = Map.of(
            BendingElement.WATER,
                    List.of(BendingElement.ICE, BendingElement.PLANT, BendingElement.HEALING, BendingElement.BLOOD),
            BendingElement.EARTH, List.of(BendingElement.SAND, BendingElement.METAL, BendingElement.LAVA),
            BendingElement.FIRE, List.of(BendingElement.LIGHTNING, BendingElement.COMBUSTION, BendingElement.BLUE_FIRE),
            BendingElement.AIR, List.of(BendingElement.SPIRITUAL, BendingElement.FLIGHT));

    /** Called every server tick; banks one attuned tick per held base element. */
    public static void tick(MinecraftServer server) {
        if (!Config.ENABLE_BENDING.get() || !Config.MASTERY_ENABLED.get()) {
            return;
        }
        long priceTicks = Config.msToTicks(Config.MASTERY_ATTUNEMENT_MS.get());
        if (priceTicks <= 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            try {
                tickPlayer(player, priceTicks);
            } catch (RuntimeException ignored) {
                // A mastery hiccup must never take the tick down.
            }
        }
    }

    private static void tickPlayer(ServerPlayer player, long priceTicks) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.isToggled()) {
            return;
        }
        if (bending.elements().contains(BendingElement.AVATAR)) {
            return;
        }
        for (Map.Entry<BendingElement, List<BendingElement>> entry : ORDER.entrySet()) {
            BendingElement base = entry.getKey();
            if (!bending.elements().contains(base)) {
                continue;
            }
            bending.addAttunement(base, 1);
            long earned = bending.attunement().getOrDefault(base, 0L) / priceTicks;
            List<BendingElement> order = entry.getValue();
            long owned =
                    bending.elements().stream().filter(e -> e.parent() == base).count();
            while (owned < earned && owned < order.size()) {
                BendingElement sub = order.get((int) owned);
                if (bending.addElement(sub)) {
                    announce(player, sub);
                }
                owned++;
            }
        }
    }

    private static void announce(ServerPlayer player, BendingElement sub) {
        player.displayClientMessage(
                Component.literal(
                        "Attunement complete — the " + sub.key() + " art answers you now. Bind it with /au bind."),
                false);
        player.serverLevel()
                .playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.0F);
        BendingBoardSync.sync(player);
    }
}
