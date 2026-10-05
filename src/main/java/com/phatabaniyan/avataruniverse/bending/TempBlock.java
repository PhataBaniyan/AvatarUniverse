package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of ProjectKorra {@code TempBlock}. Korra bends real blocks: the
 * original {@link BlockState} is stored, the position is replaced (water,
 * earth, ice, ...), and the original is restored when the ability ends.
 * Every instance registers in a global tracker so logout, death-cancel and
 * server stop can never leak a changed block into the world.
 *
 * <p>Anti-spread note: water/air TempBlocks use {@link #QUIET} (clients-only,
 * no neighbor notify). Flag 3 would ping adjacent natural water every
 * place+revert cycle, and that lake water — not ours, never reverted — would
 * flow into the gaps. Combined with 1-tick churn (every travelling block is
 * reverted and rebuilt each tick, so flow timers never complete — the same
 * trick Korra's rebuild relies on), bent water cannot spread. The
 * {@code FluidPlaceBlockEvent} guard separately covers liquid mixing.</p>
 */
public final class TempBlock {
    /** setBlock flags: sync clients without notifying neighbors (no flow triggers). */
    public static final int QUIET = Block.UPDATE_CLIENTS;

    private static final Set<TempBlock> ACTIVE = ConcurrentHashMap.newKeySet();
    /**
     * Position index for O(1) lookups (Korra's per-block instance map). The
     * first live entry owns the cell; later stacked entries are tracked in
     * {@link #ACTIVE} only. Revert removes the mapping only if it still
     * points at the reverting instance.
     */
    private record CellKey(ServerLevel level, BlockPos pos) {}

    private static final Map<CellKey, TempBlock> BY_POS = new ConcurrentHashMap<>();

    private final ServerLevel level;
    private final BlockPos pos;
    private final BlockState original;
    private final int flags;
    private final boolean virtual;
    private BlockState replacement;
    private boolean reverted;

    public TempBlock(ServerLevel level, BlockPos pos, BlockState replacement) {
        this(level, pos, replacement, 3);
    }

    public TempBlock(ServerLevel level, BlockPos pos, BlockState replacement, int flags) {
        this.level = level;
        this.pos = pos.immutable();
        // If another live temp already owns this cell, adopt its original so
        // stacked temps (hovering Dig, revisited tunnel cells) all revert to
        // the true natural state instead of stranding AIR.
        TempBlock existing = getAt(level, this.pos);
        this.original = existing != null ? existing.original : level.getBlockState(this.pos);
        this.flags = flags;
        this.replacement = replacement;
        // Water-on-water (or identical states) needs no placement at all: the
        // look is identical, and skipping avoids orphaned flow ticks firing
        // on natural water after we leave. This covers still sources AND
        // flowing/waterlogged originals — an orphan tick on those would
        // kickstart flow in water that was sitting still, which is exactly
        // how a rotating ring smears a shoreline outward.
        this.virtual = this.original.equals(replacement)
                || (replacement.getFluidState().is(Fluids.WATER)
                        && !this.original.getFluidState().isEmpty());
        if (!this.virtual) {
            level.setBlock(this.pos, replacement, flags);
            // Kill the onPlace-scheduled fluid tick outright: temp water is
            // display-only and must never spread on its own (reference parity).
            level.getFluidTicks().clearArea(new BoundingBox(this.pos));
        }
        ACTIVE.add(this);
        BY_POS.putIfAbsent(new CellKey(level, this.pos), this);
    }

    /** Current (bent) occupancy, mutating with updateReplacement calls. */
    public boolean isAirReplacement() {
        return replacement.isAir();
    }

    /** Add/update the current bent state without losing the stored original. */
    public void updateReplacement(BlockState state) {
        if (reverted) {
            return;
        }
        level.setBlock(pos, state, flags);
        level.getFluidTicks().clearArea(new BoundingBox(pos));
        this.replacement = state;
    }

    public BlockPos pos() {
        return pos;
    }

    public ServerLevel level() {
        return level;
    }

    public boolean isReverted() {
        return reverted;
    }

    /** True while this position is bent (used to suppress vanilla fluid spread). */
    public static boolean isTemp(ServerLevel level, BlockPos pos) {
        return getAt(level, pos) != null;
    }

    /** The live entry bent at a position, if any (thawing other constructs' ice). */
    public static TempBlock getAt(ServerLevel level, BlockPos pos) {
        TempBlock indexed = BY_POS.get(new CellKey(level, pos));
        if (indexed != null && !indexed.reverted) {
            return indexed;
        }
        return null;
    }

    /**
     * Restore the original block (Korra reverts unconditionally; a warning is
     * logged if something overwrote the temp block meanwhile). Virtual
     * entries never touched the world, so they just unregister.
     */
    public void revert() {
        if (reverted) {
            return;
        }
        reverted = true;
        ACTIVE.remove(this);
        BY_POS.remove(new CellKey(level, pos), this);
        if (virtual) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (!current.getFluidState().isSource() && !current.isAir()) {
            AvatarUniverseMod.LOGGER.warn("TempBlock at {} was overwritten, restoring original anyway", pos);
        }
        level.setBlock(pos, original, flags);
        // Silent revert: no neighbor wake, no fluid reschedule (reference parity).
        level.getFluidTicks().clearArea(new BoundingBox(pos));
    }

    /**
     * Sweep every tracked cell's pending fluid ticks (reference parity).
     * Neighbor edits, explosions or other mods can re-wake flow through
     * paths no event covers; the fluid delay always exceeds this per-tick
     * sweep. Called from the server tick.
     *
     * <p>Water temps also sweep their 1-block neighborhood (Korra
     * {@code AFFECTED_BLOCKS} flow guards): natural rapids beside a bent
     * source would otherwise run the infinite-water check against it and
     * convert to sources along the ability's path.
     */
    public static void suppressFlowTicks() {
        for (TempBlock temp : Set.copyOf(ACTIVE)) {
            if (temp.reverted || temp.virtual) {
                continue;
            }
            if (temp.replacement.getFluidState().is(Fluids.WATER)) {
                temp.level.getFluidTicks().clearArea(new BoundingBox(temp.pos).inflatedBy(1));
            } else {
                temp.level.getFluidTicks().clearArea(new BoundingBox(temp.pos));
            }
        }
    }

    /** Emergency revert for server stop (Korra: TempBlock cleanup onDisable). */
    public static void revertAll() { // Copy to avoid concurrent modification: revert() removes from ACTIVE.
        for (TempBlock temp : Set.copyOf(ACTIVE)) {
            try {
                temp.revert();
            } catch (Exception e) {
                AvatarUniverseMod.LOGGER.warn("Failed reverting TempBlock at {}", temp.pos, e);
            }
        }
    }

    public static int activeCount() {
        return ACTIVE.size();
    }
}
