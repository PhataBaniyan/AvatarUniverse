package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Flow-guard parity with the reference implementation: temp cells are
 * display-only water and must never be re-woken by neighbor updates.
 * Cancelling the notify stops updateShape from rescheduling fluid ticks on
 * them. Survival punches must not break them either (no drops, no stale
 * registry entries), and falling blocks landing on them are discarded so
 * consumed holes stay open.
 */
@EventBusSubscriber(modid = AvatarUniverseMod.MODID)
public final class BendingGuards {
    private BendingGuards() {}

    @SubscribeEvent
    static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (TempBlock.isTemp(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (TempBlock.isTemp(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getEntity() instanceof FallingBlockEntity falling)) {
            return;
        }
        if (falling.getTags().contains(com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.TAG)) {
            // Accretion blocks never place permanently: cancel native
            // placement and stamp a timed TempBlock with their block type.
            com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.land(
                    level, event.getPos(), event.getPlacedBlock(), falling);
            event.setCanceled(true);
            return;
        }
        if (falling.getTags().contains(com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.TAG)) {
            // EarthSurf wave lumps are steering visuals: never let them place
            // permanently. The ability mirrors them with block displays and
            // respawns the pair if one is lost.
            event.setCanceled(true);
            falling.discard();
            return;
        }
        if (falling.getTags().contains(com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.TAG)) {
            // EarthShard volleys burst on touch: never place, never duplicate.
            event.setCanceled(true);
            falling.discard();
            return;
        }
        if (!TempBlock.isTemp(level, event.getPos())) {
            return;
        }
        event.setCanceled(true);
        falling.discard();
    }
}
