package com.phatabaniyan.avataruniverse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phatabaniyan.avataruniverse.bending.BendingBlocks;
import com.phatabaniyan.avataruniverse.bending.BentWaterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws the bent-water overlay: the marker block itself is invisible, so the
 * scaled water cube is the whole visual. Offset and scale come from the
 * block entity (server-set per cell).
 */
public class BentWaterRenderer implements BlockEntityRenderer<BentWaterBlockEntity> {
    public BentWaterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BentWaterBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay) {
        boolean dirt = blockEntity.kind() == BentWaterBlockEntity.KIND_DIRT;
        BlockState state = dirt
                ? net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()
                : BendingBlocks.BENT_WATER.get().defaultBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5 + blockEntity.pullX(), 0.5 + blockEntity.pullY(), 0.5 + blockEntity.pullZ());
        poseStack.scale(blockEntity.scaleX(), blockEntity.scaleY(), blockEntity.scaleZ());
        poseStack.translate(-0.5, -0.5, -0.5);
        // Batched so the cube lands on the right layer outright: dirt is
        // opaque, the water cube blends translucent like real water.
        if (blockEntity.getLevel() != null) {
            Minecraft.getInstance()
                    .getBlockRenderer()
                    .renderBatched(
                            state,
                            blockEntity.getBlockPos(),
                            blockEntity.getLevel(),
                            poseStack,
                            buffer.getBuffer(dirt ? RenderType.solid() : RenderType.translucent()),
                            true,
                            net.minecraft.util.RandomSource.create());
        }
        poseStack.popPose();
    }
}
