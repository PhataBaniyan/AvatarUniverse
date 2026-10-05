package com.phatabaniyan.avataruniverse.bending;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marker for a BER-scaled bent-water visual. Carries the overlay offset and
 * scale; the server sets both at placement and they sync via the standard
 * update packet.
 */
public class BentWaterBlockEntity extends BlockEntity {
    /** Blue water-cube overlay. */
    public static final int KIND_WATER = 0;
    /** Vanilla dirt-cube overlay. */
    public static final int KIND_DIRT = 1;

    private float pullX;
    private float pullY;
    private float pullZ;
    private float scaleX = 1.0F;
    private float scaleY = 1.0F;
    private float scaleZ = 1.0F;
    private int kind = KIND_WATER;

    public BentWaterBlockEntity(BlockPos pos, BlockState state) {
        super(BendingBlocks.BENT_WATER_BE.get(), pos, state);
    }

    /** One-shot visual setup (single setChanged, single sync). */
    public void setVisual(float pullX, float pullZ, float scaleX, float scaleY, float scaleZ) {
        setVisualFull(pullX, 0.0F, pullZ, scaleX, scaleY, scaleZ, KIND_WATER);
    }

    /** One-shot visual setup with vertical pull and overlay kind (single sync). */
    public void setVisualFull(
            float pullX, float pullY, float pullZ, float scaleX, float scaleY, float scaleZ, int kind) {
        if (this.pullX == pullX
                && this.pullY == pullY
                && this.pullZ == pullZ
                && this.scaleX == scaleX
                && this.scaleY == scaleY
                && this.scaleZ == scaleZ
                && this.kind == kind) {
            return;
        }
        this.pullX = pullX;
        this.pullY = pullY;
        this.pullZ = pullZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.kind = kind;
        setChanged();
    }

    public float pullX() {
        return pullX;
    }

    public float pullY() {
        return pullY;
    }

    public float pullZ() {
        return pullZ;
    }

    public float scaleX() {
        return scaleX;
    }

    public float scaleY() {
        return scaleY;
    }

    public float scaleZ() {
        return scaleZ;
    }

    public int kind() {
        return kind;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("PullX", pullX);
        tag.putFloat("PullY", pullY);
        tag.putFloat("PullZ", pullZ);
        tag.putFloat("ScaleX", scaleX);
        tag.putFloat("ScaleY", scaleY);
        tag.putFloat("ScaleZ", scaleZ);
        tag.putInt("Kind", kind);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pullX = tag.getFloat("PullX");
        pullY = tag.getFloat("PullY");
        pullZ = tag.getFloat("PullZ");
        scaleX = tag.contains("ScaleX") ? tag.getFloat("ScaleX") : 1.0F;
        scaleY = tag.contains("ScaleY") ? tag.getFloat("ScaleY") : 1.0F;
        scaleZ = tag.contains("ScaleZ") ? tag.getFloat("ScaleZ") : 1.0F;
        kind = tag.getInt("Kind");
    }
}
