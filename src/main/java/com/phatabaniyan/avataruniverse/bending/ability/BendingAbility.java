package com.phatabaniyan.avataruniverse.bending.ability;

import java.util.UUID;

/**
 * Port of ProjectKorra's ability base ({@code CoreAbility} progress model).
 * Korra's BendingManager ticks every ability each server tick; here
 * {@link com.phatabaniyan.avataruniverse.bending.BendingManager} does the same
 * from {@code ServerTickEvent.Post}. Return false from {@link #progress} when
 * the ability finishes so it is removed, mirroring Korra's remove().
 */
public abstract class BendingAbility {
    protected final UUID owner;
    protected final long startTime;

    protected BendingAbility(UUID owner, long startTime) {
        this.owner = owner;
        this.startTime = startTime;
    }

    public UUID owner() {
        return owner;
    }

    /** Display name used for slot binding and cooldown keys (Korra: getName()). */
    public abstract String name();

    /**
     * Advance one server tick.
     *
     * @return true to keep alive, false to remove
     */
    public abstract boolean progress();

    /**
     * Cleanup hook, always called when the ability is removed (finished,
     * cancelled, owner logged out). Override to revert {@code TempBlock}s.
     */
    public void onRemove() {}
}
