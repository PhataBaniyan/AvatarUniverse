package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block + block-entity registrations (server-safe; no client classes here). */
public final class BendingBlocks {
    private BendingBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AvatarUniverseMod.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AvatarUniverseMod.MODID);

    /**
     * Invisible marker bent water shows through a client overlay (BER-scaled
     * water cube). Never placed by hand: TempBlocks own every cell and revert
     * them. No item form.
     */
    public static final DeferredBlock<Block> BENT_WATER = BLOCKS.register(
            "bent_water",
            () -> new BentWaterBlock(Block.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .instabreak()
                    .noLootTable()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BentWaterBlockEntity>> BENT_WATER_BE =
            BLOCK_ENTITY_TYPES.register(
                    "bent_water", () -> BlockEntityType.Builder.of(BentWaterBlockEntity::new, BENT_WATER.get())
                            .build(null));
}
