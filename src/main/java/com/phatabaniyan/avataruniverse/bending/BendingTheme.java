package com.phatabaniyan.avataruniverse.bending;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Per-element full-sentence gradients for every command message, ported from
 * ProjectAvatar {@code ElementTheme} + {@code GradientText}. Fire runs red to
 * orange, earth green to brown, water deep blue to ice, air white to silver,
 * chi yellow to ember, avatar purple to cyan.
 */
public final class BendingTheme {
    private BendingTheme() {}

    public static int start(BendingElement element) {
        if (element == null) {
            return 0xFFFFFF;
        }
        return switch (element) {
            case FIRE -> 0xFF5A1A;
            case WATER -> 0x3D8BFF;
            case EARTH -> 0x7DBA4D;
            case AIR -> 0xFFFFFF;
            case AVATAR -> 0xB45FD6;
            case FLIGHT -> 0xFFFFFF;
            case SPIRITUAL -> 0xD4BCFF;
            case BLOOD -> 0x8A1A1A;
            case HEALING -> 0xFFC9DE;
            case ICE -> 0x9ADDFF;
            case PLANT -> 0x2A6B2E;
            case LAVA -> 0xFF7526;
            case METAL -> 0x93A6B2;
            case SAND -> 0xD8AC62;
            case LIGHTNING -> 0xFFF79B;
            case COMBUSTION -> 0x6E1D1D;
            case BLUE_FIRE -> 0x4D79FF;
        };
    }

    public static int end(BendingElement element) {
        if (element == null) {
            return 0xFFFFFF;
        }
        return switch (element) {
            case FIRE -> 0xFFC933;
            case WATER -> 0xA8E8FF;
            case EARTH -> 0xBFA89B;
            case AIR -> 0xE8EDF3;
            case AVATAR -> 0x7FF7FF;
            case FLIGHT -> 0xA8B8C2;
            case SPIRITUAL -> 0x8A5FFF;
            case BLOOD -> 0xE02434;
            case HEALING -> 0xC4F2F8;
            case ICE -> 0xFFFFFF;
            case PLANT -> 0x4CA657;
            case LAVA -> 0xFFE082;
            case METAL -> 0xFFFFFF;
            case SAND -> 0xFFEBAD;
            case LIGHTNING -> 0xFF8A26;
            case COMBUSTION -> 0xFF6F42;
            case BLUE_FIRE -> 0x8AE8FF;
        };
    }

    /**
     * Flame particles for a caster: soul-blue for explicit blue-fire holders,
     * plain orange otherwise.
     */
    public static net.minecraft.core.particles.ParticleOptions flame(java.util.UUID owner) {
        BendingPlayer bending = owner == null ? null : BendingPlayer.get(owner);
        if (bending != null && bending.elements().contains(BendingElement.BLUE_FIRE)) {
            return net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME;
        }
        return net.minecraft.core.particles.ParticleTypes.FLAME;
    }

    /**
     * Config-driven particle type: resolves a {@code minecraft:name} id to a
     * simple particle, falling back when unknown or data-bearing (dust,
     * block, item need extra data commands cannot express).
     */
    public static net.minecraft.core.particles.ParticleOptions particle(
            String id, net.minecraft.core.particles.ParticleOptions fallback) {
        try {
            var type = net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.get(
                    net.minecraft.resources.ResourceLocation.parse(id));
            if (type instanceof net.minecraft.core.particles.SimpleParticleType simple) {
                return simple;
            }
        } catch (RuntimeException ignored) {
            // Unknown id: the fallback renders instead.
        }
        return fallback;
    }

    /**
     * Flame emissions: blue-fire holders always burn soul-blue; everyone else
     * uses the configured id (or the fallback when it cannot resolve).
     */
    public static net.minecraft.core.particles.ParticleOptions particle(
            String id, java.util.UUID owner, net.minecraft.core.particles.ParticleOptions fallback) {
        BendingPlayer bending = owner == null ? null : BendingPlayer.get(owner);
        if (bending != null && bending.elements().contains(BendingElement.BLUE_FIRE)) {
            return net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME;
        }
        return particle(id, fallback);
    }

    public static MutableComponent gradient(String text, BendingElement element) {
        if (element == null) {
            return gradient(text, 0xFFFFFF, 0xFFFFFF);
        }
        return gradient(text, start(element), end(element));
    }

    /** Whole sentence shaded between two raw colors. */
    public static MutableComponent gradient(String text, int from, int to) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        MutableComponent out = Component.empty();
        int n = text.length();
        for (int i = 0; i < n; i++) {
            float t = n == 1 ? 0.0F : (float) i / (float) (n - 1);
            int rgb = lerp(from, to, t);
            out.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true)));
        }
        return out;
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** Element behind a bound ability name, for tinting its messages. */
    public static BendingElement elementOfAbility(String ability) {
        if (ability == null) {
            return null;
        }
        return switch (ability.toLowerCase(java.util.Locale.ROOT)) {
            case "watermanipulation",
                    "torrent",
                    "torrentburst",
                    "waterspout",
                    "waterarms",
                    "waterbubble",
                    "phasechange",
                    "drain",
                    "wakefishing" -> BendingElement.WATER;
            case "frostbreath", "iceblast", "icespike", "iceclaws", "icewall", "icecrawl" -> BendingElement.ICE;
            case "razorleaf", "plantarmor", "leafstorm" -> BendingElement.PLANT;
                // Runtime sub-binds (WaterArms / PlantArmor shells): tinted for
                // boards and bars. "Grapple" serves both shells, so it stays
                // unmapped and falls back to the live parent.
            case "pull", "punch", "grab", "spear" -> BendingElement.WATER;
            case "vinewhip", "tangle", "leap", "leafshield", "leafdome", "regenerate", "disperse" -> BendingElement
                    .PLANT;
            case "bloodbending", "bloodpuppet" -> BendingElement.BLOOD;
            case "healingwaters" -> BendingElement.HEALING;
            case "firejet",
                    "firekick",
                    "firespin",
                    "firewheel",
                    "firedisc",
                    "fireball",
                    "fireski",
                    "walloffire",
                    "illumination",
                    "fireburst",
                    "fireshield",
                    "flamebreath",
                    "firebreath",
                    "firewave",
                    "firecomet",
                    "fireshots",
                    "firemanipulation",
                    "heatcontrol",
                    "jets" -> BendingElement.FIRE;
            case "arcspark",
                    "chargebolt",
                    "bolt",
                    "discharge",
                    "lightningburst",
                    "lightning",
                    "electrify" -> BendingElement.LIGHTNING;
            case "combustbeam", "explode", "combustionblast", "combustion" -> BendingElement.COMBUSTION;
            case "airjet",
                    "airscooter",
                    "airspout",
                    "airpunch",
                    "airslam",
                    "airflight",
                    "airshield",
                    "airburst",
                    "airblast",
                    "airswipe",
                    "airsuction",
                    "suffocate",
                    "tornado",
                    "airbreath",
                    "airbullet",
                    "meditate",
                    "sonicblast",
                    "zephyr",
                    "airstream" -> BendingElement.AIR;
            case "avatarstate", "elementsphere", "spiritbeam" -> BendingElement.AVATAR;
            case "spiritgrasp", "spiritprojection", "spiritstep" -> BendingElement.SPIRITUAL;
            case "accretion",
                    "catapult",
                    "earthblast",
                    "eartharmor",
                    "collapsewall",
                    "raiseearth",
                    "shockwave",
                    "earthkick",
                    "quickweld",
                    "rockslide",
                    "shrapnel",
                    "earthpillar",
                    "earthshard",
                    "fissure",
                    "earthglove",
                    "earthdome",
                    "dig",
                    "earthtunnel",
                    "earthsurf",
                    "earthgrab" -> BendingElement.EARTH;
            case "lavaflow", "lavasurge", "lavadisc", "lavaflux", "lavadisk" -> BendingElement.LAVA;
            case "metalclips",
                    "metalfragments",
                    "metalhook",
                    "metalarmor",
                    "magnetshield",
                    "extraction" -> BendingElement.METAL;
            case "mudsurge" -> BendingElement.EARTH;
            default -> null;
        };
    }
}
