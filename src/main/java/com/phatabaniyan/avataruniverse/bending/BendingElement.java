package com.phatabaniyan.avataruniverse.bending;

import java.util.Locale;

/**
 * Port of ProjectKorra {@code Element} (core/src/.../Element.java).
 * Bukkit has no client/server split; here elements are pure data usable on both sides.
 * Subelements (Flight, Blood, Metal, Lava, Lightning, ...) arrive as separate
 * abilities gated by these base elements in a later port step.
 */
public enum BendingElement {
    AIR("air", 0xD8D8D8),
    WATER("water", 0x3B7DE0),
    EARTH("earth", 0x7A5A2E),
    FIRE("fire", 0xE03B2E),
    AVATAR("avatar", 0x9B59B6),

    FLIGHT("flight", 0xF5F5F5, AIR),
    SPIRITUAL("spiritual", 0xC3A6FF, AIR),
    BLOOD("blood", 0x5E0000, WATER),
    HEALING("healing", 0xF8BBD0, WATER),
    ICE("ice", 0x7FD4FF, WATER),
    PLANT("plant", 0x14401A, WATER),
    LAVA("lava", 0xFF5A00, EARTH),
    METAL("metal", 0x78909C, EARTH),
    SAND("sand", 0xC8954B, EARTH),
    LIGHTNING("lightning", 0xFFF176, FIRE),
    COMBUSTION("combustion", 0x4A1010, FIRE),
    BLUE_FIRE("bluefire", 0x1A53FF, FIRE);

    private final String key;
    private final int color;
    private final BendingElement parent;

    BendingElement(String key, int color) {
        this(key, color, null);
    }

    BendingElement(String key, int color, BendingElement parent) {
        this.key = key;
        this.color = color;
        this.parent = parent;
    }

    public String key() {
        return key;
    }

    /** Parent element, or null for base elements (Korra sub-element model). */
    public BendingElement parent() {
        return parent;
    }

    public boolean isSub() {
        return parent != null;
    }

    public int color() {
        return color;
    }

    /** Permission suffix, e.g. bending.air (Korra: plugin.yml bending.air node, default true). */
    public String permissionNode() {
        return "bending." + key;
    }

    public static BendingElement byName(String name) {
        if (name == null) {
            return null;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        for (BendingElement element : values()) {
            if (element.key.equals(lower) || element.name().equalsIgnoreCase(lower)) {
                return element;
            }
        }
        return null;
    }
}
