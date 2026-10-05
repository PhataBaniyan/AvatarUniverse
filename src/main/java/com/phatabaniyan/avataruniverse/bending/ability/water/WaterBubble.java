package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code WaterBubble}: a breathable air pocket pushed
 * through water. Click grows a short timed bubble; pressing sneak with it
 * bound holds the pocket for as long as sneak is held, following the player
 * wherever they go, and melts on release. Re-clicking near the bubble
 * refreshes it (follows the player, adopts the latest sneak state, extends
 * the click timer) instead of popping it; re-clicking far away melts the old
 * one while a new one grows. Water cleared inside (plus waterlogged blocks
 * drained), everything restored after. Follows the player, pruning behind it.
 */
public class WaterBubble extends BendingAbility {
    public static final String ID = "WaterBubble";

    /**
     * Prune hysteresis: tracked cells survive this far past the pocket rim.
     * A walking player's fractional center drifts across block edges every
     * tick, and exact-radius pruning strobes the rim (revert + re-clear each
     * crossing). Truly abandoned cells still melt once left behind.
     */
    private static final double PRUNE_MARGIN = 1.5;

    private final ServerLevel level;
    private boolean shiftMode;
    private long bornTick;
    private boolean removing;
    private double radius;
    private Vec3 center;
    private final Map<BlockPos, TempBlock> cleared = new HashMap<>();

    public WaterBubble(ServerPlayer player, boolean shiftMode) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.shiftMode = shiftMode;
        this.bornTick = player.level().getGameTime();
        this.radius = 0.0;
        this.center = player.position();
    }

    /** The eye block must be breathable when MustStartAboveWater is enabled. */
    public static boolean canStart(ServerPlayer player) {
        if (!Config.WATERBUBBLE_MUST_START_ABOVE_WATER.get()) {
            return true;
        }
        ServerLevel level = player.serverLevel();
        BlockPos eye = BlockPos.containing(player.getEyePosition());
        var state = level.getBlockState(eye);
        return !state.isSolid() && state.getFluidState().isEmpty();
    }

    /**
     * Re-click refresh (Korra constructor refresh): the bubble follows the
     * player, adopts the latest sneak state and restarts the click timer.
     */
    public void refresh(ServerPlayer player, boolean shiftMode) {
        this.center = player.position();
        this.shiftMode = shiftMode;
        this.bornTick = player.level().getGameTime();
        this.removing = false;
    }

    /** Start melting (Korra far re-click): the old bubble shrinks on its own. */
    public void startRemoving() {
        this.removing = true;
    }

    /** True while this bubble keeps the position breathable (for other abilities). */
    public boolean covers(BlockPos pos) {
        return cleared.containsKey(pos.immutable());
    }

    /** Current bubble center (re-click range check). */
    public Vec3 center() {
        return center;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isRemoved() || player.isDeadOrDying()) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        boolean valid = bending != null
                && bending.hasElement(com.phatabaniyan.avataruniverse.bending.BendingElement.WATER)
                && bending.isToggled()
                && player.serverLevel().equals(level);
        if (!valid) {
            removing = true;
        }
        // Sneak-hold self-heals: a held sneak bubble can never stay latched
        // from a transient hiccup, so it keeps clearing wherever the player
        // goes until sneak is actually released.
        if (shiftMode && player.isShiftKeyDown() && valid) {
            removing = false;
        }
        if (shiftMode && !player.isShiftKeyDown()) {
            removing = true;
        }
        long now = level.getGameTime();
        double maxRadius = Config.WATERBUBBLE_RADIUS.get();
        double speed = Config.WATERBUBBLE_SPEED.get();
        if (!shiftMode && now - bornTick > Config.WATERBUBBLE_CLICK_DURATION_TICKS.get()) {
            removing = true;
        }
        if (maxRadius < radius) {
            removing = true;
        }
        if (removing) {
            radius -= speed;
            if (radius <= 0.1) {
                return false;
            }
        } else if (radius < maxRadius) {
            radius = Math.min(maxRadius, radius + speed);
        }
        center = player.position();
        syncCells();
        if (Config.ENABLE_DEBUG_LOGGING.get() && now % 40 == 0) {
            AvatarUniverseMod.LOGGER.info(
                    "WaterBubble owner={} radius={} removing={} shift={} tracked={} feet={}",
                    owner,
                    String.format("%.2f", radius),
                    removing,
                    shiftMode,
                    cleared.size(),
                    player.position());
        }
        return true;
    }

    /** Diff-update the cleared sphere around the player. */
    private void syncCells() {
        Set<BlockPos> want = new HashSet<>();
        int bound = (int) Math.ceil(radius) + 1;
        BlockPos centerBlock = BlockPos.containing(center);
        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    BlockPos pos = centerBlock.offset(ox, oy, oz);
                    // Tracked cells first (Korra waterOrigins check): they are
                    // temp-air right now, so a world-state test would evict
                    // and re-clear them every tick (visible flicker). A
                    // tracked cell reverted out from under us (other ability,
                    // scheduled revert) falls through for re-capture.
                    TempBlock tracked = cleared.get(pos);
                    if (tracked != null && !tracked.isReverted()) {
                        want.add(pos.immutable());
                        // Backstop for fluid paths that bypass the place
                        // event: plain water back under a live claim is
                        // re-cleared in place (the claim still restores the
                        // original later), so the pocket never goes patchy.
                        var current = level.getBlockState(pos);
                        if (current.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER)
                                && !(current.hasProperty(BlockStateProperties.WATERLOGGED)
                                        && current.getValue(BlockStateProperties.WATERLOGGED))) {
                            level.setBlock(
                                    pos.immutable(),
                                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                                    TempBlock.QUIET);
                            level.getFluidTicks()
                                    .clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(
                                            pos.immutable()));
                        }
                        continue;
                    }
                    if (tracked != null) {
                        cleared.remove(pos);
                    }
                    double dx = pos.getX() + 0.5 - center.x;
                    double dy = pos.getY() + 0.5 - center.y;
                    double dz = pos.getZ() + 0.5 - center.z;
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    var state = level.getBlockState(pos);
                    boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                            && state.getValue(BlockStateProperties.WATERLOGGED);
                    if (state.getFluidState().isEmpty() && !waterlogged) {
                        continue;
                    }
                    want.add(pos.immutable());
                }
            }
        }
        for (Map.Entry<BlockPos, TempBlock> entry : new java.util.ArrayList<>(cleared.entrySet())) {
            BlockPos tracked = entry.getKey();
            double tdx = tracked.getX() + 0.5 - center.x;
            double tdy = tracked.getY() + 0.5 - center.y;
            double tdz = tracked.getZ() + 0.5 - center.z;
            double pruneR = radius + PRUNE_MARGIN;
            if (!want.contains(tracked) && tdx * tdx + tdy * tdy + tdz * tdz > pruneR * pruneR) {
                entry.getValue().revert();
                cleared.remove(tracked);
            }
        }
        for (BlockPos pos : want) {
            if (cleared.containsKey(pos)) {
                continue;
            }
            // Held by another construct (melting older bubble, wave trail):
            // never stack two temps on one cell, capture it once released.
            if (TempBlock.isTemp(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            BlockState drained = state;
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                    && state.getValue(BlockStateProperties.WATERLOGGED)) {
                drained = state.setValue(BlockStateProperties.WATERLOGGED, false);
            } else {
                drained = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
            cleared.put(pos, new TempBlock(level, pos, drained, TempBlock.QUIET));
        }
        // Halo freeze: plain spread fires no event (verified against the
        // patched FlowingFluid bytecode), so no guard can intercept inflow.
        // Clearing scheduled fluid ticks over the pocket plus one block out
        // starves neighbouring flows before they fire, instead of warring
        // with them tick-for-tick. Player-caused wakes are still re-cleared
        // above; paused surroundings resume on the next vanilla block update.
        int halo = bound + 1;
        level.getFluidTicks()
                .clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(
                        centerBlock.getX() - halo,
                        centerBlock.getY() - halo,
                        centerBlock.getZ() - halo,
                        centerBlock.getX() + halo,
                        centerBlock.getY() + halo,
                        centerBlock.getZ() + halo));
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new java.util.ArrayList<>(cleared.values())) {
            temp.revert();
        }
        cleared.clear();
    }
}
