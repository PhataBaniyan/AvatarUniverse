package com.phatabaniyan.avataruniverse.bending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Pushes board state to clients: full refresh on demand (login, bind,
 * element change) plus a light periodic tick so cooldowns stay truthful.
 */
public final class BendingBoardSync {
    private BendingBoardSync() {}

    private static final Map<java.util.UUID, Integer> FORCE_SLOT = new java.util.concurrent.ConcurrentHashMap<>();

    /** Park the caster's hotbar on a slot; consumed by the next sync. */
    public static void queueForceSlot(java.util.UUID owner, int slot) {
        FORCE_SLOT.put(owner, slot);
    }

    public static void sync(ServerPlayer player) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null) {
            return;
        }
        // Forced slot applies server-side first (binds and modes read it),
        // then mirrors to the client in the same payload.
        Integer forced = FORCE_SLOT.remove(player.getUUID());
        if (forced != null) {
            player.getInventory().selected = forced;
        }
        long gameTime = player.level().getGameTime();
        List<String> elements = new ArrayList<>();
        boolean avatar = false;
        for (BendingElement element : bending.elements()) {
            if (element == BendingElement.AVATAR) {
                avatar = true;
            } else {
                elements.add(element.key());
            }
        }
        if (avatar) {
            // Avatar sits above all: its colors lead the board.
            elements.add(0, BendingElement.AVATAR.key());
        }
        List<String> binds = new ArrayList<>();
        for (int slot = 1; slot <= 9; slot++) {
            String bound = bending.boundAbility(slot);
            binds.add(bound == null ? "" : bound);
        }
        Map<String, Long> cooldowns = new HashMap<>();
        for (Map.Entry<String, Long> entry : bending.cooldownSnapshot().entrySet()) {
            long remainingTicks = entry.getValue() - gameTime;
            if (remainingTicks > 0) {
                cooldowns.put(entry.getKey(), remainingTicks * 50L);
            }
        }
        List<Integer> sphereUses = new ArrayList<>();
        com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere sphere = BendingManager.find(
                player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.class);
        if (sphere != null) {
            sphereUses.add(sphere.getAirUses());
            sphereUses.add(sphere.getEarthUses());
            sphereUses.add(sphere.getFireUses());
            sphereUses.add(sphere.getWaterUses());
        }
        SubBoard sub = subBoard(player);
        PacketDistributor.sendToPlayer(
                player,
                new BendingBoardPayload(
                        elements,
                        binds,
                        cooldowns,
                        sphereUses,
                        sub.title(),
                        sub.element(),
                        sub.labels(),
                        sub.colors(),
                        sub.notes(),
                        forced == null ? -1 : forced));
    }

    private record SubBoard(
            String title, String element, List<String> labels, List<String> colors, List<String> notes) {
        static SubBoard none() {
            return new SubBoard("", "", List.of(), List.of(), List.of());
        }
    }

    /** Sub-board for abilities with modes, after the source flight board. */
    private static SubBoard subBoard(ServerPlayer player) {
        java.util.UUID owner = player.getUUID();
        com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight flight =
                BendingManager.find(owner, com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.class);
        if (flight != null) {
            String mode = flight.modeName();
            String speed = flight.speedName();
            List<String> labels = List.of("Soar", "Glide", "Levitate", "Ending");
            List<String> colors = List.of("air", "air", "air", "air");
            List<String> notes = new ArrayList<>();
            for (String label : labels) {
                if (label.equals(mode)) {
                    notes.add("◀" + (label.equals("Soar") ? " " + speed : ""));
                } else {
                    notes.add(label.equals("Soar") ? speed : "");
                }
            }
            return new SubBoard("Flight", "air", labels, colors, notes);
        }
        com.phatabaniyan.avataruniverse.bending.ability.water.WaterArms arms =
                BendingManager.find(owner, com.phatabaniyan.avataruniverse.bending.ability.water.WaterArms.class);
        if (arms != null) {
            List<String> labels = List.of(com.phatabaniyan.avataruniverse.bending.ability.water.WaterArms.SUB_BINDS);
            return new SubBoard(
                    "WaterArms",
                    "water",
                    labels,
                    java.util.Collections.nCopies(labels.size(), "water"),
                    java.util.Collections.nCopies(labels.size(), ""));
        }
        com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor shell =
                BendingManager.find(owner, com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.class);
        if (shell != null) {
            List<String> labels = List.of(com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.SUBS);
            return new SubBoard(
                    "PlantArmor",
                    "plant",
                    labels,
                    java.util.Collections.nCopies(labels.size(), "plant"),
                    java.util.Collections.nCopies(labels.size(), ""));
        }
        com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk disk =
                BendingManager.find(owner, com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.class);
        if (disk != null) {
            String mode = disk.diskMode();
            List<String> labels = List.of("Follow", "Advance", "Return", "Rotate", "Shatter");
            List<String> notes = new ArrayList<>();
            for (String label : labels) {
                notes.add(label.equals(mode) ? "◀" : "");
            }
            return new SubBoard(
                    "LavaDisk", "lava", labels, java.util.Collections.nCopies(labels.size(), "lava"), notes);
        }
        return SubBoard.none();
    }
}
