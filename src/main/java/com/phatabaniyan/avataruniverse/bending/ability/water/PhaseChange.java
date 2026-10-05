package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code PhaseChange}: click freezes the surface of
 * water around your gaze into lasting ice (melted back only when you wander
 * out of range), while holding sneak melts ice and snow around your gaze
 * (which refreezes when you let go). Cross-ability thaw hooks, sounds and
 * night factors are cut with the reference's shared systems.
 */
public class PhaseChange extends BendingAbility {
    public static final String ID = "PhaseChange";

    public enum Mode {
        FREEZE,
        MELT
    }

    private final ServerLevel level;
    private final Mode mode;
    private final Map<BlockPos, TempBlock> frozen = new HashMap<>();
    private final List<BlockPos> melted = new ArrayList<>();
    private int meltRadius = 1;
    private double meltTicks;

    public PhaseChange(ServerPlayer player, Mode mode) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.mode = mode;
    }

    /** Click burst (Korra freezeArea): ice the surface water around the gaze target. */
    public boolean freezeBurst(ServerPlayer player) {
        if (mode != Mode.FREEZE) {
            return false;
        }
        long now = level.getGameTime();
        if (BendingPlayer.get(owner) != null && BendingPlayer.get(owner).isOnCooldown("PhaseChangeFreeze", now)) {
            return false;
        }
        Vec3 target = WaterSpoutWave.gazeTarget(player, Config.PHASE_SOURCE_RANGE.get());
        BlockPos center = BlockPos.containing(target);
        double radius = Config.PHASE_FREEZE_RADIUS.get();
        int depth = Config.PHASE_FREEZE_DEPTH.get();
        int bound = (int) Math.ceil(radius);
        boolean any = false;
        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    double dx = ox;
                    double dy = oy;
                    double dz = oz;
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    for (int d = 0; d < depth; d++) {
                        BlockPos pos = center.offset(ox, oy - d, oz);
                        if (tryFreeze(pos)) {
                            any = true;
                        }
                    }
                }
            }
        }
        if (any) {
            BendingPlayer bending = BendingPlayer.get(owner);
            if (bending != null) {
                bending.setCooldown("PhaseChangeFreeze", now + Config.msToTicks(Config.PHASE_FREEZE_COOLDOWN_MS.get()));
            }
        }
        return any;
    }

    private boolean tryFreeze(BlockPos pos) {
        if (!level.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER)) {
            return false;
        }
        if (TempBlock.isTemp(level, pos)) {
            return false;
        }
        if (frozen.containsKey(pos.immutable())) {
            return false;
        }
        boolean airAbove = false;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isAir()) {
                airAbove = true;
                break;
            }
        }
        if (!airAbove) {
            return false;
        }
        TempBlock ice = new TempBlock(level, pos.immutable(), Blocks.ICE.defaultBlockState(), TempBlock.QUIET);
        frozen.put(pos.immutable(), ice);
        return true;
    }

    @Override
    public String name() {
        return ID;
    }

    /** True for the sneak-held melt mode (freeze bursts share the class). */
    public boolean isMelt() {
        return mode == Mode.MELT;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isRemoved() || player.isDeadOrDying()) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null || !bending.hasElement(BendingElement.WATER) || !bending.isToggled()) {
            return false;
        }
        if (mode == Mode.FREEZE) {
            double controlSqr = Config.PHASE_FREEZE_CONTROL_RADIUS.get() * Config.PHASE_FREEZE_CONTROL_RADIUS.get();
            for (Map.Entry<BlockPos, TempBlock> entry : new java.util.ArrayList<>(frozen.entrySet())) {
                if (entry.getKey().distSqr(player.blockPosition()) > controlSqr) {
                    entry.getValue().revert();
                    frozen.remove(entry.getKey());
                }
            }
            return !frozen.isEmpty();
        }
        if (!player.isShiftKeyDown()
                || !ID.equalsIgnoreCase(bending.boundAbility(player.getInventory().selected + 1))) {
            endMelt(bending);
            return false;
        }
        if (meltRadius >= Config.PHASE_MELT_RADIUS.get()) {
            meltRadius = 1;
        }
        Vec3 target = WaterSpoutWave.gazeTarget(player, Config.PHASE_SOURCE_RANGE.get());
        meltArea(BlockPos.containing(target), meltRadius);
        return true;
    }

    private void meltArea(BlockPos center, int radius) {
        List<BlockPos> ice = new ArrayList<>();
        int bound = radius + 3;
        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    BlockPos pos = center.offset(ox, oy, oz);
                    var state = level.getBlockState(pos);
                    if (BendingSources.isIce(level, pos) || BendingSources.isSnow(level, pos)) {
                        ice.add(pos.immutable());
                    } else if (state.isAir()) {
                        continue;
                    }
                }
            }
        }
        meltTicks += Config.PHASE_MELT_SPEED.get() / 20.0;
        for (int i = 0; i < (int) (meltTicks % Config.PHASE_MELT_SPEED.get()); i++) {
            if (ice.isEmpty()) {
                meltRadius++;
                return;
            }
            BlockPos pos = ice.remove(level.random.nextInt(ice.size()));
            melt(pos);
        }
    }

    private void melt(BlockPos pos) {
        var state = level.getBlockState(pos);
        boolean nether = level.dimension() == Level.NETHER;
        if (TempBlock.isTemp(level, pos)) {
            if (BendingSources.isIce(level, pos) || BendingSources.isSnow(level, pos)) {
                TempBlock existing = TempBlock.getAt(level, pos);
                if (existing != null) {
                    existing.revert();
                }
            }
            return;
        }
        if (state.is(Blocks.SNOW)) {
            int layers = state.getValue(BlockStateProperties.LAYERS);
            BlockState replacement = layers <= 1
                    ? Blocks.AIR.defaultBlockState()
                    : state.setValue(BlockStateProperties.LAYERS, layers - 1);
            BendingManager.scheduleRevert(
                    new TempBlock(level, pos.immutable(), replacement, TempBlock.QUIET),
                    level.getGameTime() + Config.msToTicks(Config.PHASE_SNOW_MELT_MS.get()));
            return;
        }
        if (BendingSources.isSnow(level, pos)) {
            BendingManager.scheduleRevert(
                    new TempBlock(level, pos.immutable(), Blocks.AIR.defaultBlockState(), TempBlock.QUIET),
                    level.getGameTime() + Config.msToTicks(Config.PHASE_SNOW_MELT_MS.get()));
            return;
        }
        if (!BendingSources.isIce(level, pos)) {
            return;
        }
        if (nether) {
            level.setBlock(pos.immutable(), Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
            return;
        }
        if (Config.PHASE_MELT_ALLOW_FLOW.get()) {
            level.setBlock(pos.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET);
            melted.add(pos.immutable());
        } else {
            BendingManager.scheduleRevert(
                    new TempBlock(level, pos.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET),
                    level.getGameTime() + Config.msToTicks(Config.PHASE_SNOW_MELT_MS.get()));
            melted.add(pos.immutable());
        }
    }

    private void endMelt(BendingPlayer bending) {
        bending.setCooldown(
                "PhaseChangeMelt", level.getGameTime() + Config.msToTicks(Config.PHASE_MELT_COOLDOWN_MS.get()));
        meltRadius = 1;
        meltTicks = 0;
    }

    @Override
    public void onRemove() {
        for (Map.Entry<BlockPos, TempBlock> entry : new java.util.ArrayList<>(frozen.entrySet())) {
            entry.getValue().revert();
        }
        frozen.clear();
        if (mode == Mode.MELT) {
            for (BlockPos pos : new ArrayList<>(melted)) {
                if (level.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER)
                        && !TempBlock.isTemp(level, pos)) {
                    level.setBlock(pos, Blocks.ICE.defaultBlockState(), TempBlock.QUIET);
                }
            }
        }
        melted.clear();
    }
}
