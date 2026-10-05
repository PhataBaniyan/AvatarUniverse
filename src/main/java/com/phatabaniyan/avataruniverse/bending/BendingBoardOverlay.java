package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Aesthetic bending board, after ProjectAvatar's overlay: a framed right-side
 * panel with gradient element rails, all nine binds tinted per element, live
 * cooldown seconds, selected-row highlight, and a gradient cooldown bar
 * under the crosshair for the held slot.
 */
@EventBusSubscriber(modid = AvatarUniverseMod.MODID, value = Dist.CLIENT)
public final class BendingBoardOverlay {
    private static final int BAR_W = 72;
    private static final int BAR_H = 4;

    private BendingBoardOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        int forced = ClientBoard.consumeForceSlot();
        if (forced >= 0 && forced <= 8) {
            mc.player.getInventory().selected = forced;
        }
        List<String> elementKeys = ClientBoard.elements();
        if (elementKeys.isEmpty()) {
            return;
        }
        BendingElement primary = BendingElement.byName(elementKeys.get(0));
        if (primary == null) {
            return;
        }
        List<BendingElement> owned = new ArrayList<>();
        for (String key : elementKeys) {
            BendingElement element = BendingElement.byName(key);
            if (element != null) {
                owned.add(element);
            }
        }
        GuiGraphics g = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        drawCooldownBar(mc, g, primary, sw, sh);
        if (ClientBoard.sphereUses().size() == 4) {
            drawSphereBoard(g, mc, sw, sh);
        } else if (!ClientBoard.subTitle().isEmpty() && !ClientBoard.subLabels().isEmpty()) {
            drawSubBoard(g, mc, sw, sh);
        } else {
            drawBoard(g, mc, owned, primary, sw, sh);
        }
    }

    /** Generic sub-board for modal abilities, tinted by the art's element. */
    private static void drawSubBoard(GuiGraphics g, Minecraft mc, int sw, int sh) {
        int selected = mc.player.getInventory().selected + 1;
        BendingElement frame = BendingElement.byName(ClientBoard.subElement());
        if (frame == null) {
            frame = BendingElement.AIR;
        }
        List<String> labels = ClientBoard.subLabels();
        List<String> colors = ClientBoard.subColors();
        List<String> notes = ClientBoard.subNotes();
        int rows = labels.size();
        int lineH = 9;
        int pad = 6;
        Component title = BendingTheme.gradient(ClientBoard.subTitle(), frame);
        int maxText = mc.font.width(title);
        for (int k = 0; k < rows; k++) {
            BendingElement rowElement = BendingElement.byName(k < colors.size() ? colors.get(k) : "");
            if (rowElement == null) {
                rowElement = frame;
            }
            maxText = Math.max(
                    maxText,
                    9
                            + mc.font.width(BendingTheme.gradient(labels.get(k), rowElement))
                            + 4
                            + mc.font.width("◀ NORMAL"));
        }
        int w = Math.min(190, Math.max(70, maxText + pad * 2));
        int inner = w - pad * 2 - 9;
        int h = pad * 2 + 11 + rows * lineH;
        int x = sw - w - 8;
        int y = (sh - h) / 2;
        g.fill(x, y, x + w, y + h, 0x90000000);
        gradientBar(g, x, y, w, BendingTheme.start(frame), BendingTheme.end(frame));
        gradientBar(g, x, y + h - 2, w, BendingTheme.start(frame), BendingTheme.end(frame));
        g.drawString(mc.font, title, x + (w - mc.font.width(title)) / 2, y + pad, 0xFFFFFF, true);
        int ly = y + pad + 12;
        for (int k = 0; k < rows; k++) {
            int slot = k + 1;
            if (slot == selected) {
                g.fill(x + 3, ly - 1, x + w - 3, ly + lineH - 1, 0x60FFFFFF);
            }
            BendingElement rowElement = BendingElement.byName(k < colors.size() ? colors.get(k) : "");
            if (rowElement == null) {
                rowElement = frame;
            }
            g.drawString(mc.font, BendingTheme.gradient(String.valueOf(slot), rowElement), x + pad, ly, 0xFFFFFF, true);
            String note = k < notes.size() ? notes.get(k) : "";
            int reserved = note.isEmpty() ? 0 : 4 + mc.font.width(note);
            String shown = ellipsize(mc, labels.get(k), rowElement, inner - reserved);
            g.drawString(mc.font, BendingTheme.gradient(shown, rowElement), x + pad + 9, ly, 0xFFFFFF, true);
            if (!note.isEmpty()) {
                int tw = mc.font.width(note);
                g.drawString(mc.font, note, x + w - pad - tw, ly, note.startsWith("◀") ? 0xFFFFFF : 0x707070, true);
            }
            ly += lineH;
        }
    }

    private static BendingElement elementOf(String ability, BendingElement fallback) {
        BendingElement element = BendingTheme.elementOfAbility(ability);
        if (element == null) {
            return fallback;
        }
        // Tint by the viewer's own sub-element when it refines the art.
        for (String key : ClientBoard.elements()) {
            BendingElement owned = BendingElement.byName(key);
            if (owned != null && owned.parent() == element) {
                return owned;
            }
        }
        return element;
    }

    private static int mixStart(List<BendingElement> owned, BendingElement fallback) {
        return mix(owned, fallback, true);
    }

    private static int mixEnd(List<BendingElement> owned, BendingElement fallback) {
        return mix(owned, fallback, false);
    }

    private static int mix(List<BendingElement> owned, BendingElement fallback, boolean start) {
        if (owned.isEmpty()) {
            return start ? BendingTheme.start(fallback) : BendingTheme.end(fallback);
        }
        long r = 0;
        long g = 0;
        long b = 0;
        for (BendingElement element : owned) {
            int rgb = start ? BendingTheme.start(element) : BendingTheme.end(element);
            r += (rgb >> 16) & 0xFF;
            g += (rgb >> 8) & 0xFF;
            b += rgb & 0xFF;
        }
        int n = owned.size();
        return (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n);
    }

    private static void drawCooldownBar(Minecraft mc, GuiGraphics g, BendingElement primary, int sw, int sh) {
        int slot = mc.player.getInventory().selected + 1;
        String bound = ClientBoard.bind(slot);
        if (bound == null || bound.isEmpty()) {
            return;
        }
        long remaining = ClientBoard.remainingMs(bound);
        if (remaining <= 0) {
            return;
        }
        float done = ClientBoard.fractionDone(bound);
        BendingElement element = elementOf(bound, primary);
        int x = (sw - BAR_W) / 2;
        int y = sh - 49;
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0x80000000);
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF101010);
        int filled = Math.round(BAR_W * done);
        int segs = Math.min(Math.max(filled, 1), 12);
        for (int s = 0; s < segs; s++) {
            int x0 = x + (filled * s) / segs;
            int x1 = x + (filled * (s + 1)) / segs;
            float t = segs <= 1 ? 0.0F : (float) s / (float) (segs - 1);
            g.fill(x0, y, x1, y + BAR_H, 0xFF000000 | lerp(BendingTheme.start(element), BendingTheme.end(element), t));
        }
        Component label = BendingTheme.gradient(capitalize(bound), element);
        int tw = mc.font.width(label);
        g.drawString(mc.font, label, (sw - tw) / 2, y - 11, 0xFFFFFF, true);
    }

    private static void drawBoard(
            GuiGraphics g, Minecraft mc, List<BendingElement> owned, BendingElement primary, int sw, int sh) {
        int selected = mc.player.getInventory().selected + 1;
        int lineH = 9;
        int pad = 6;
        Component title =
                BendingTheme.gradient(capitalize(primary.key()), mixStart(owned, primary), mixEnd(owned, primary));
        int maxText = mc.font.width(title);
        java.util.Map<Integer, BendingElement> rowElements = new java.util.HashMap<>();
        for (int i = 1; i <= 9; i++) {
            String ability = ClientBoard.bind(i);
            BendingElement rowElement = elementOf(ability, primary);
            rowElements.put(i, rowElement);
            int rowW;
            if (ability == null || ability.isEmpty()) {
                rowW = 9 + mc.font.width("- -");
            } else {
                rowW = 9 + mc.font.width(BendingTheme.gradient(capitalize(ability), rowElement));
                if (ClientBoard.remainingMs(ability) > 0) {
                    rowW += 4 + mc.font.width("1200s");
                }
            }
            maxText = Math.max(maxText, rowW);
        }
        int w = Math.min(170, Math.max(70, maxText + pad * 2));
        int inner = w - pad * 2 - 9;
        int h = pad * 2 + 11 + 9 * lineH;
        int x = sw - w - 8;
        int y = (sh - h) / 2;
        g.fill(x, y, x + w, y + h, 0x90000000);
        gradientBar(g, x, y, w, mixStart(owned, primary), mixEnd(owned, primary));
        gradientBar(g, x, y + h - 2, w, mixStart(owned, primary), mixEnd(owned, primary));
        g.drawString(mc.font, title, x + (w - mc.font.width(title)) / 2, y + pad, 0xFFFFFF, true);
        int ly = y + pad + 12;
        for (int i = 1; i <= 9; i++) {
            String ability = ClientBoard.bind(i);
            if (i == selected) {
                g.fill(x + 3, ly - 1, x + w - 3, ly + lineH - 1, 0x60FFFFFF);
            }
            BendingElement rowElement = rowElements.getOrDefault(i, primary);
            g.drawString(mc.font, BendingTheme.gradient(String.valueOf(i), rowElement), x + pad, ly, 0xFFFFFF, true);
            if (ability == null || ability.isEmpty()) {
                g.drawString(mc.font, "- -", x + pad + 9, ly, 0x707070, false);
            } else {
                long remaining = ClientBoard.remainingMs(ability);
                String secs = remaining > 0 ? ((remaining + 999) / 1000) + "s" : "";
                int reserved = secs.isEmpty() ? 0 : 4 + mc.font.width(secs);
                String shown = ellipsize(mc, capitalize(ability), rowElement, inner - reserved);
                g.drawString(mc.font, BendingTheme.gradient(shown, rowElement), x + pad + 9, ly, 0xFFFFFF, true);
                if (!secs.isEmpty()) {
                    int tw = mc.font.width(secs);
                    g.drawString(mc.font, secs, x + w - pad - tw, ly, 0xFF8888, true);
                }
            }
            ly += lineH;
        }
    }

    /**
     * Clip a row label with an ellipsis so text never escapes the frame.
     * Measured on the styled (bold) gradient, since bold runs ~1px wider per
     * character than the plain string.
     */
    private static String ellipsize(Minecraft mc, String text, BendingElement element, int maxWidth) {
        if (mc.font.width(BendingTheme.gradient(text, element)) <= maxWidth) {
            return text;
        }
        String dots = "…";
        while (text.length() > 1 && mc.font.width(BendingTheme.gradient(text + dots, element)) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + dots;
    }

    /** Live-sphere sub-board: five elements with remaining hurls. */
    private static void drawSphereBoard(GuiGraphics g, Minecraft mc, int sw, int sh) {
        int selected = mc.player.getInventory().selected + 1;
        String[] modes = {"Air", "Earth", "Fire", "Water", "Stream"};
        BendingElement[] colors = {
            BendingElement.AIR, BendingElement.EARTH, BendingElement.FIRE, BendingElement.WATER, BendingElement.AVATAR
        };
        List<Integer> uses = ClientBoard.sphereUses();
        int lineH = 9;
        int pad = 6;
        Component title = BendingTheme.gradient("Sphere", BendingElement.AVATAR);
        int maxText = mc.font.width(title);
        for (int k = 0; k < 5; k++) {
            maxText = Math.max(
                    maxText, 9 + mc.font.width(BendingTheme.gradient(modes[k], colors[k])) + 4 + mc.font.width("×12"));
        }
        int w = Math.min(190, Math.max(70, maxText + pad * 2));
        int h = pad * 2 + 11 + 5 * lineH;
        int x = sw - w - 8;
        int y = (sh - h) / 2;
        g.fill(x, y, x + w, y + h, 0x90000000);
        gradientBar(g, x, y, w, BendingTheme.start(BendingElement.AVATAR), BendingTheme.end(BendingElement.AVATAR));
        gradientBar(
                g, x, y + h - 2, w, BendingTheme.start(BendingElement.AVATAR), BendingTheme.end(BendingElement.AVATAR));
        g.drawString(mc.font, title, x + (w - mc.font.width(title)) / 2, y + pad, 0xFFFFFF, true);
        int streamLeft = Math.min(Math.min(uses.get(0), uses.get(1)), Math.min(uses.get(2), uses.get(3)));
        int ly = y + pad + 12;
        for (int k = 0; k < 5; k++) {
            int slot = k + 1;
            if (slot == selected) {
                g.fill(x + 3, ly - 1, x + w - 3, ly + lineH - 1, 0x60FFFFFF);
            }
            g.drawString(mc.font, BendingTheme.gradient(String.valueOf(slot), colors[k]), x + pad, ly, 0xFFFFFF, true);
            g.drawString(mc.font, BendingTheme.gradient(modes[k], colors[k]), x + pad + 9, ly, 0xFFFFFF, true);
            String left = "×" + (k == 4 ? streamLeft : uses.get(k));
            int tw = mc.font.width(left);
            g.drawString(mc.font, left, x + w - pad - tw, ly, 0xD0D0D0, true);
            ly += lineH;
        }
    }

    private static void gradientBar(GuiGraphics g, int x, int y, int w, int start, int end) {
        int segs = Math.min(w, 16);
        for (int s = 0; s < segs; s++) {
            int x0 = x + (w * s) / segs;
            int x1 = x + (w * (s + 1)) / segs;
            float t = segs <= 1 ? 0.0F : (float) s / (float) (segs - 1);
            g.fill(x0, y, x1, y + 2, 0xFF000000 | lerp(start, end, t));
        }
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        return Math.round(ar + (br - ar) * t) << 16
                | Math.round(ag + (bg - ag) * t) << 8
                | Math.round(ab + (bb - ab) * t);
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
