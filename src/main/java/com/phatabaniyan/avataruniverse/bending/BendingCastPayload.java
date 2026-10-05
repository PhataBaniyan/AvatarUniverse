package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server cast request. Needed because left-clicking air sends no
 * vanilla packet, so no server-side interact event fires for it. The server
 * re-validates everything (element, binding, source, cooldown) in
 * {@link BendingEvents#tryCast(ServerPlayer)}.
 */
public record BendingCastPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BendingCastPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(AvatarUniverseMod.MODID, "bending_cast"));

    public static final StreamCodec<FriendlyByteBuf, BendingCastPayload> STREAM_CODEC =
            StreamCodec.unit(new BendingCastPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BendingCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BendingEvents.tryCast(player);
            }
        });
    }
}
