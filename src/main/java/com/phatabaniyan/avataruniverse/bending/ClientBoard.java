package com.phatabaniyan.avataruniverse.bending;

import java.util.List;
import java.util.Map;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-side mirror of the bending board state, refreshed by
 * {@link BendingBoardPayload}. Cooldowns count down locally from the
 * remaining milliseconds stamped at receipt.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientBoard {
    private static List<String> elements = List.of();
    private static List<String> binds = List.of("", "", "", "", "", "", "", "", "");
    private static Map<String, Long> cooldowns = Map.of();
    private static List<Integer> sphereUses = List.of();
    private static String subTitle = "";
    private static String subElement = "";
    private static List<String> subLabels = List.of();
    private static List<String> subColors = List.of();
    private static List<String> subNotes = List.of();
    private static int forceSlot = -1;
    private static final java.util.Map<String, Long> peaks = new java.util.HashMap<>();
    private static long receivedAtMs;

    private ClientBoard() {}

    static void update(BendingBoardPayload payload) {
        elements = List.copyOf(payload.elements());
        binds = List.copyOf(payload.binds());
        cooldowns = Map.copyOf(payload.cooldowns());
        sphereUses = List.copyOf(payload.sphereUses());
        subTitle = payload.subTitle();
        subElement = payload.subElement();
        subLabels = List.copyOf(payload.subLabels());
        subColors = List.copyOf(payload.subColors());
        subNotes = List.copyOf(payload.subNotes());
        forceSlot = payload.forceSlot();
        receivedAtMs = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            peaks.merge(entry.getKey(), entry.getValue(), Math::max);
        }
        peaks.keySet().retainAll(cooldowns.keySet());
    }

    public static List<String> elements() {
        return elements;
    }

    public static String bind(int slot) {
        if (slot < 1 || slot > binds.size()) {
            return "";
        }
        return binds.get(slot - 1);
    }

    /** Remaining milliseconds on an ability, or 0 when ready. */
    public static long remainingMs(String ability) {
        Long stamped = cooldowns.get(ability);
        if (stamped == null) {
            return 0L;
        }
        return Math.max(0L, stamped - (System.currentTimeMillis() - receivedAtMs));
    }

    /** Live sphere uses [air, earth, fire, water], or empty when no sphere. */
    public static List<Integer> sphereUses() {
        return sphereUses;
    }

    public static String subTitle() {
        return subTitle;
    }

    public static String subElement() {
        return subElement;
    }

    public static List<String> subLabels() {
        return subLabels;
    }

    public static List<String> subColors() {
        return subColors;
    }

    public static List<String> subNotes() {
        return subNotes;
    }

    /** Forced hotbar slot from the server, -1 when none (consumed on read). */
    public static int consumeForceSlot() {
        int slot = forceSlot;
        forceSlot = -1;
        return slot;
    }

    /** Fraction elapsed (0 fresh, 1 ready) for the cooldown bar. */
    public static float fractionDone(String ability) {
        long remaining = remainingMs(ability);
        if (remaining <= 0) {
            return 1.0F;
        }
        long peak = Math.max(peaks.getOrDefault(ability, remaining), remaining);
        return 1.0F - (float) remaining / (float) peak;
    }
}
