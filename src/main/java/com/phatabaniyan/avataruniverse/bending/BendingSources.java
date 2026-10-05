package com.phatabaniyan.avataruniverse.bending;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * What a waterbender can draw from (ProjectKorra water/ice/plant sourcing).
 * Flowing water counts alongside still sources; every ice and snow variant
 * counts; ferns and all leaves count via plantbending. One predicate shared
 * by the select packet, the cast gate, the client picker and the focus
 * shimmer so they can never disagree.
 */
public final class BendingSources {
    private static final Set<Block> ICE = Set.of(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE);

    private static final Set<Block> SNOW = Set.of(Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);

    private static final Set<Block> PLANTS = Set.of(
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.MANGROVE_LEAVES,
            Blocks.CHERRY_LEAVES,
            Blocks.AZALEA_LEAVES,
            Blocks.FLOWERING_AZALEA_LEAVES);

    private BendingSources() {}

    public static boolean isWaterSource(BlockGetter level, BlockPos pos) {
        if (level.getFluidState(pos).is(Fluids.WATER)) {
            return true;
        }
        Block block = level.getBlockState(pos).getBlock();
        return ICE.contains(block) || SNOW.contains(block) || PLANTS.contains(block);
    }

    /** Ice/snow sources fire an icy bolt instead of a water bolt. */
    public static boolean isIce(BlockGetter level, BlockPos pos) {
        return ICE.contains(level.getBlockState(pos).getBlock())
                || SNOW.contains(level.getBlockState(pos).getBlock());
    }

    /** Plant sources are consumed on cast and regrow after a delay. */
    public static boolean isPlant(BlockGetter level, BlockPos pos) {
        return PLANTS.contains(level.getBlockState(pos).getBlock());
    }

    /** Snow sources behave like plants for consumption (Korra PlantRegrowth). */
    public static boolean isSnow(BlockGetter level, BlockPos pos) {
        return SNOW.contains(level.getBlockState(pos).getBlock());
    }

    /**
     * Positions a bending wave/ring may occupy (Korra transparency): air,
     * replaceable blocks (grass, snow layers, torches) and fluids. Never
     * containers (would drop/destroy stored items on replace).
     */
    public static boolean isTransparentForBend(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container) {
            return false;
        }
        var state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty();
    }
}
