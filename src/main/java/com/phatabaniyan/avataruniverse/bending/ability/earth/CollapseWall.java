package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code CollapseWall}
 * (core/.../earthbending/CollapseWall.java from the local
 * ProjectKorra-master copy). Sneak to raise a 7-wide, 6-high earth wall
 * ahead: it grows layer by layer and stands only while sneak is held,
 * reverting the moment sneak is released. Cooldown 10t
 * ("Collapse.Wall.Cooldown" 500ms).
 */
public class CollapseWall extends EarthAbility {
    public static final String ID = "CollapseWall";
    /** Reference Wall.Cooldown 500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = ((long) Config.msToTicks(Config.COLLAPSEWALL_COOLDOWN_MS.get()));

    private static final int HEIGHT = Config.COLLAPSEWALL_HEIGHT.get();
    private static final int HALF_WIDTH = Config.COLLAPSEWALL_HALF_WIDTH.get();
    private static final long LAYER_INTERVAL_TICKS = Config.msToTicks(Config.COLLAPSEWALL_LAYER_INTERVAL_MS.get());
    private static final double DISTANCE = Config.COLLAPSEWALL_DISTANCE.get();

    private int layersBuilt;
    private long lastLayerAt;
    private boolean started;
    /** Birth tick, for subclasses that stand a fixed time instead of holding sneak. */
    protected final long bornAt;

    private final Map<BlockPos, BlockState> wallStates = new HashMap<>();
    /** Locked on the first layer so moving mid-sneak can't stagger the wall. */
    private List<BlockPos> wallColumns;

    public CollapseWall(ServerPlayer player) {
        super(player);
        this.bornAt = player.level().getGameTime();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: bendable ground must be in reach ahead. */
    public static boolean canBegin(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            flat = new Vec3(0, 0, 1);
        }
        Vec3 base = player.position().add(flat.normalize().scale(DISTANCE));
        BlockPos ground = findGround(player, BlockPos.containing(base));
        return ground != null && Accretion.isEarthbendable(player.serverLevel(), ground);
    }

    private static BlockPos findGround(ServerPlayer player, BlockPos probe) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = player.serverLevel().getBlockState(cell);
            if (!state.isAir() && !isSoftVegetation(state)) {
                return cell.immutable();
            }
        }
        return null;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!started) {
            started = true;
            buildLayer(0);
            lastLayerAt = level.getGameTime();
            cool(owner, player, cooldownKey(), COOLDOWN_TICKS);
        } else if (layersBuilt < HEIGHT && level.getGameTime() - lastLayerAt >= LAYER_INTERVAL_TICKS) {
            lastLayerAt = level.getGameTime();
            buildLayer(layersBuilt);
        }
        return keepStanding();
    }

    /** CollapseWall stands only while sneak is held; subclasses may stand a fixed time. */
    protected boolean keepStanding() {
        return player.isShiftKeyDown();
    }

    /** Cooldown key, so subclasses cool their own id. */
    protected String cooldownKey() {
        return ID;
    }

    /** Raise one wall layer at a time so the wall visibly grows. */
    private void buildLayer(int layer) {
        if (wallColumns == null) {
            wallColumns = frameColumns();
            if (wallColumns.isEmpty()) {
                return;
            }
        }
        for (BlockPos column : wallColumns) {
            if (!Accretion.isEarthbendable(level, column) && !movedEarth.containsKey(column)) {
                continue;
            }
            if (layer == 0) {
                wallStates.putIfAbsent(column, level.getBlockState(column));
            }
            BlockState columnState = wallStates.getOrDefault(column, Blocks.STONE.defaultBlockState());
            if (columnState.isAir()) {
                continue;
            }
            BlockPos cell = column.above(layer + 1);
            BlockState occupying = level.getBlockState(cell);
            if (TempBlock.isTemp(level, cell) || (!occupying.isAir() && !isSoftVegetation(occupying))) {
                continue;
            }
            if (!occupying.isAir()) {
                remember(cell);
                level.setBlock(cell, Blocks.AIR.defaultBlockState(), 2);
            }
            remember(cell);
            level.setBlock(cell, columnState, 2);
            if (layer == 0) {
                remember(column);
                level.setBlock(column, Blocks.AIR.defaultBlockState(), 2);
            }
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, columnState),
                    cell.getX() + 0.5,
                    cell.getY() + 0.5,
                    cell.getZ() + 0.5,
                    3,
                    0.3,
                    0.3,
                    0.3,
                    0.02);
        }
        layersBuilt = Math.max(layersBuilt, layer + 1);
    }

    /**
     * Lock the 7 ground columns once, from the sneak-start position and
     * facing, so later layers land on the same cells even if the player
     * strafes mid-sneak. Rounded lateral steps keep them contiguous on
     * diagonals; per-column ground sampling keeps them flush on slopes.
     */
    private List<BlockPos> frameColumns() {
        List<BlockPos> columns = new ArrayList<>();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            flat = new Vec3(0, 0, 1);
        }
        flat = flat.normalize();
        Vec3 side = new Vec3(-flat.z, 0, flat.x).normalize();
        Vec3 base = player.position().add(flat.scale(DISTANCE));
        BlockPos ground = findGround(BlockPos.containing(base));
        if (ground == null || !Accretion.isEarthbendable(level, ground)) {
            return columns;
        }
        for (int s = -HALF_WIDTH; s <= HALF_WIDTH; s++) {
            BlockPos columnBase = ground.offset((int) Math.round(side.x * s), 0, (int) Math.round(side.z * s));
            BlockPos column = findGround(columnBase.above(2));
            if (column != null) {
                columns.add(column);
            }
        }
        return columns;
    }

    /** Vegetation/snow/replaceables may be cleared for the wall; solids may not. */
    private static boolean isSoftVegetation(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        return !state.isSolid()
                || key.contains("LEAVES")
                || key.contains("FLOWER")
                || key.contains("GRASS")
                || key.contains("VINE")
                || key.contains("SNOW");
    }

    private BlockPos findGround(BlockPos probe) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            if (!state.isAir() && !isSoftVegetation(state)) {
                return cell.immutable();
            }
        }
        return null;
    }

    @Override
    public void onRemove() {
        revertMovedEarth();
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
