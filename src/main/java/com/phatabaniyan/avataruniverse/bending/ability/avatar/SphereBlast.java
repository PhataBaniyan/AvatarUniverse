package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code avatar.sphere.SphereBlast}: spherical crater
 * helper shared by the earth and stream finisher impacts. Broken cells become
 * AIR tracked as TempBlocks and revert after the delay (ms converted to
 * ticks), mirroring the reference TempRevert behavior.
 */
public final class SphereBlast {
    private SphereBlast() {}

    public static void crater(ServerLevel level, Vec3 center, int radius, long revertMs) {
        BlockPos c = BlockPos.containing(center);
        long revertAt = level.getGameTime() + Math.max(1L, revertMs / 50L);
        for (BlockPos pos :
                BlockPos.betweenClosed(c.offset(-radius, -radius, -radius), c.offset(radius, radius, radius))) {
            if (pos.distSqr(c) > (double) radius * radius) {
                continue;
            }
            var state = level.getBlockState(pos);
            if (state.isAir() || state.is(BlockTags.WITHER_IMMUNE)) {
                continue;
            }
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                continue;
            }
            BlockPos immutable = pos.immutable();
            TempBlock temp = new TempBlock(level, immutable, Blocks.AIR.defaultBlockState());
            if (revertMs > 0) {
                BendingManager.scheduleRevert(temp, revertAt);
            }
        }
    }
}
