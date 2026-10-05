package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import net.minecraft.server.level.ServerPlayer;

/**
 * Port of ProjectKorra {@code RaiseEarth} wall mode
 * (core/.../earthbending/RaiseEarth.java, plus RaiseEarthWall, from the
 * local ProjectKorra-master copy). Reference instructions: tap sneak on an
 * earthbendable block to raise a wall of earth. Same sneak-start, same
 * layer-by-layer growth and same wall as {@link CollapseWall} (Wall Height
 * 6, Width 6, Speed 10, Cooldown 500ms), except the wall stands on its own
 * for 600 ticks (30s) after raising and then reverts, instead of vanishing
 * when sneak is released.
 */
public class RaiseEarth extends CollapseWall {
    public static final String ID = "RaiseEarth";
    /** Stand time before the wall reverts, in server ticks. */
    private static final long STAND_TICKS = Config.msToTicks(Config.RAISEEARTH_STAND_MS.get());

    public RaiseEarth(ServerPlayer player) {
        super(player);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    protected String cooldownKey() {
        return ID;
    }

    @Override
    protected boolean keepStanding() {
        return level.getGameTime() - bornAt < STAND_TICKS;
    }
}
