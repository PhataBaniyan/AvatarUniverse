package com.phatabaniyan.avataruniverse.bending;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

/**
 * Port of ProjectKorra {@code BendingPlayer} (in-memory half).
 * Korra persists to SQLite/MySQL + supports offline players; that half is
 * explicitly out of scope here (see skill section 9: per-player YAML/DB ->
 * attachment/SavedData redesign). This registry is server-side only and is
 * loaded on login, dropped on logout, copied on death clone.
 */
public final class BendingPlayer {
    private static final Map<UUID, BendingPlayer> REGISTRY = new ConcurrentHashMap<>();

    private final UUID uuid;
    private final Set<BendingElement> elements = EnumSet.noneOf(BendingElement.class);
    private final Map<Integer, String> slots = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    /** Attuned online ticks per base element (mastery progress toward sub-elements). */
    private final Map<BendingElement, Long> attunement = new EnumMap<>(BendingElement.class);

    private boolean toggled = true;

    private boolean bottledSource;
    private long lastFeedbackTime = Long.MIN_VALUE;
    /** Korra water-source selection: tapped water block the next bolt fires from. */
    private BlockPos selectedSource;

    private long selectedAt;

    private BendingPlayer(UUID uuid) {
        this.uuid = uuid;
        for (int slot = 1; slot <= 9; slot++) {
            slots.put(slot, null);
        }
    }

    public static BendingPlayer getOrCreate(UUID uuid) {
        return REGISTRY.computeIfAbsent(uuid, BendingPlayer::new);
    }

    public static BendingPlayer get(UUID uuid) {
        return REGISTRY.get(uuid);
    }

    public static void remove(UUID uuid) {
        REGISTRY.remove(uuid);
    }

    /** Copy persistent fields across death clone (Korra: PlayerRespawnEvent handling). */
    public static void copyTo(UUID from, UUID to) {
        if (from.equals(to)) {
            // Death respawns keep the UUID: the entry is already intact.
            return;
        }
        BendingPlayer source = REGISTRY.get(from);
        if (source == null) {
            return;
        }
        BendingPlayer target = getOrCreate(to);
        target.elements.clear();
        target.elements.addAll(source.elements);
        target.attunement.clear();
        target.attunement.putAll(source.attunement);
        target.slots.clear();
        target.slots.putAll(source.slots);
        target.toggled = source.toggled;
        target.cooldowns.clear();
    }

    public UUID uuid() {
        return uuid;
    }

    public Set<BendingElement> elements() {
        return Collections.unmodifiableSet(elements);
    }

    public boolean addElement(BendingElement element) {
        return elements.add(element);
    }

    public boolean removeElement(BendingElement element) {
        return elements.remove(element);
    }

    public void clearElements() {
        elements.clear();
    }

    public boolean hasElement(BendingElement element) {
        if (elements.contains(BendingElement.AVATAR) || elements.contains(element)) {
            return true;
        }
        if (!element.isSub()) {
            // A sub-element holder counts as its main-element bender
            // (ice -> water), but a main element grants no subs.
            for (BendingElement owned : elements) {
                if (owned.parent() == element) {
                    return true;
                }
            }
        }
        return false;
    }

    public void bind(int slot, String ability) {
        if (slot >= 1 && slot <= 9) {
            slots.put(slot, ability);
        }
    }

    public String boundAbility(int slot) {
        return slots.get(slot);
    }

    public Map<Integer, String> slots() {
        return Collections.unmodifiableMap(slots);
    }

    public Map<BendingElement, Long> attunement() {
        return Collections.unmodifiableMap(attunement);
    }

    /** Bank online attuned ticks toward a base element's next sub-element. */
    public void addAttunement(BendingElement base, long ticks) {
        attunement.merge(base, ticks, Long::sum);
    }

    /** Snapshot elements, binds and toggle into the vanilla player tag. */
    public void saveTo(net.minecraft.server.level.ServerPlayer player) {
        net.minecraft.nbt.CompoundTag root = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.ListTag elementsTag = new net.minecraft.nbt.ListTag();
        for (BendingElement element : elements) {
            elementsTag.add(net.minecraft.nbt.StringTag.valueOf(element.key()));
        }
        root.put("elements", elementsTag);
        net.minecraft.nbt.CompoundTag bindsTag = new net.minecraft.nbt.CompoundTag();
        for (Map.Entry<Integer, String> entry : slots.entrySet()) {
            if (entry.getValue() != null) {
                bindsTag.putString("slot" + entry.getKey(), entry.getValue());
            }
        }
        root.put("binds", bindsTag);
        root.putBoolean("toggled", toggled);
        net.minecraft.nbt.CompoundTag attuneTag = new net.minecraft.nbt.CompoundTag();
        for (Map.Entry<BendingElement, Long> entry : attunement.entrySet()) {
            attuneTag.putLong(entry.getKey().key(), entry.getValue());
        }
        root.put("attunement", attuneTag);
        player.getPersistentData().put(KEY, root);
    }

    /** Restore a snapshot written by {@link #saveTo}. */
    public void loadFrom(net.minecraft.server.level.ServerPlayer player) {
        net.minecraft.nbt.CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            return;
        }
        net.minecraft.nbt.CompoundTag root = data.getCompound(KEY);
        elements.clear();
        for (int i = 0;
                i < root.getList("elements", net.minecraft.nbt.Tag.TAG_STRING).size();
                i++) {
            BendingElement element = BendingElement.byName(
                    root.getList("elements", net.minecraft.nbt.Tag.TAG_STRING).getString(i));
            if (element != null) {
                elements.add(element);
            }
        }
        net.minecraft.nbt.CompoundTag bindsTag = root.getCompound("binds");
        for (int slot = 1; slot <= 9; slot++) {
            String bound = bindsTag.getString("slot" + slot);
            slots.put(slot, bound.isEmpty() ? null : bound);
        }
        if (root.contains("toggled", net.minecraft.nbt.Tag.TAG_BYTE)) {
            toggled = root.getBoolean("toggled");
        }
        net.minecraft.nbt.CompoundTag attuneTag = root.getCompound("attunement");
        attunement.clear();
        for (String key : attuneTag.getAllKeys()) {
            BendingElement base = BendingElement.byName(key);
            if (base != null) {
                attunement.put(base, attuneTag.getLong(key));
            }
        }
    }

    private static final String KEY = "avataruniverse";

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    /** Mark a bottled source as available (consumed water potion, Korra bottlebending). */
    public void giveBottledSource() {
        bottledSource = true;
    }

    /**
     * Take the bottled source once (torrent setup revalidation). Lingering
     * flags from earlier casts are harmless: they only skip one check.
     */
    public boolean takeBottledSource() {
        if (!bottledSource) {
            return false;
        }
        bottledSource = false;
        return true;
    }

    public boolean isOnCooldown(String ability, long gameTime) {
        return cooldownExpiresAt(ability) > gameTime;
    }

    public long cooldownExpiresAt(String ability) {
        return cooldowns.getOrDefault(ability, 0L);
    }

    /** Live cooldown table, for board sync (ability -> expires-at game time). */
    public Map<String, Long> cooldownSnapshot() {
        return Collections.unmodifiableMap(cooldowns);
    }

    public void setCooldown(String ability, long expiresAtGameTime) {
        cooldowns.put(ability, expiresAtGameTime);
    }

    /**
     * Rate-limit rejection feedback (air-click packet + held mouse button would
     * otherwise spam the actionbar). Returns true when a message may be shown.
     */
    public boolean tryFeedbackThrottle(long gameTime) {
        if (gameTime - lastFeedbackTime < 20) {
            return false;
        }
        lastFeedbackTime = gameTime;
        return true;
    }

    /** Store a tapped water source (Korra source selection). */
    public void selectSource(BlockPos pos, long gameTime) {
        selectedSource = pos.immutable();
        selectedAt = gameTime;
    }

    /**
     * The selected source, or null when none was tapped or the selection
     * expired (60s, Korra-like source memory).
     */
    public BlockPos selectedSource(long gameTime) {
        if (selectedSource == null || gameTime - selectedAt > 1200) {
            selectedSource = null;
            return null;
        }
        return selectedSource;
    }

    public void clearSource() {
        selectedSource = null;
    }
}
