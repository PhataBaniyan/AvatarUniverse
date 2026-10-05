package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Payload registration. Must run on the MOD bus (not the gameplay bus):
 * {@code RegisterPayloadHandlersEvent} is a mod-loading lifecycle event, so it
 * is wired explicitly via {@code modEventBus.addListener} in
 * {@link AvatarUniverseMod} — a gameplay-bus subscription would silently
 * never fire.
 */
public final class BendingNetworking {
    private BendingNetworking() {}

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(BendingCastPayload.TYPE, BendingCastPayload.STREAM_CODEC, BendingCastPayload::handle);
        registrar.playToServer(
                BendingSelectPayload.TYPE, BendingSelectPayload.STREAM_CODEC, BendingSelectPayload::handle);
        registrar.playToClient(BendingBoardPayload.TYPE, BendingBoardPayload.STREAM_CODEC, BendingBoardPayload::handle);
    }
}
