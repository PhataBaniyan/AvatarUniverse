package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> client bending board state: owned elements, the nine binds, and
 * live cooldowns as remaining milliseconds. The client counts down locally
 * between syncs.
 */
public record BendingBoardPayload(
        List<String> elements,
        List<String> binds,
        Map<String, Long> cooldowns,
        List<Integer> sphereUses,
        String subTitle,
        String subElement,
        List<String> subLabels,
        List<String> subColors,
        List<String> subNotes,
        int forceSlot)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BendingBoardPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(AvatarUniverseMod.MODID, "bending_board"));

    public static final StreamCodec<FriendlyByteBuf, BendingBoardPayload> STREAM_CODEC =
            StreamCodec.of(BendingBoardPayload::write, BendingBoardPayload::read);

    private static void write(FriendlyByteBuf buf, BendingBoardPayload payload) {
        buf.writeCollection(payload.elements, FriendlyByteBuf::writeUtf);
        buf.writeCollection(payload.binds, FriendlyByteBuf::writeUtf);
        buf.writeMap(payload.cooldowns, FriendlyByteBuf::writeUtf, (b, v) -> b.writeLong(v));
        buf.writeCollection(payload.sphereUses, FriendlyByteBuf::writeVarInt);
        buf.writeUtf(payload.subTitle);
        buf.writeUtf(payload.subElement);
        buf.writeCollection(payload.subLabels, FriendlyByteBuf::writeUtf);
        buf.writeCollection(payload.subColors, FriendlyByteBuf::writeUtf);
        buf.writeCollection(payload.subNotes, FriendlyByteBuf::writeUtf);
        buf.writeVarInt(payload.forceSlot);
    }

    private static BendingBoardPayload read(FriendlyByteBuf buf) {
        List<String> elements = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<String> binds = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        Map<String, Long> cooldowns = buf.readMap(HashMap::new, FriendlyByteBuf::readUtf, FriendlyByteBuf::readLong);
        List<Integer> sphereUses = buf.readCollection(ArrayList::new, FriendlyByteBuf::readVarInt);
        String subTitle = buf.readUtf();
        String subElement = buf.readUtf();
        List<String> subLabels = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<String> subColors = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<String> subNotes = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        int forceSlot = buf.readVarInt();
        return new BendingBoardPayload(
                elements,
                binds,
                cooldowns,
                sphereUses,
                subTitle,
                subElement,
                subLabels,
                subColors,
                subNotes,
                forceSlot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BendingBoardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBoard.update(payload));
    }
}
