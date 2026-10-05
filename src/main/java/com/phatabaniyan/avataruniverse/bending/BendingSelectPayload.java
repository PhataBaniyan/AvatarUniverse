package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.ability.water.Torrent;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterManipulation;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterSpout;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterSpoutWave;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server water-source selection (Korra tap-to-source). The client
 * fluid-raycasts on left-click; when it hits a bendable source within reach
 * it sends the position instead of a cast request. The server re-validates:
 * bending enabled, toggled, water element, position is still bendable, and
 * within 8 blocks of the player. Tapping with Torrent bound also readies a
 * WAITING torrent, so one click + sneak-hold is the whole flow.
 */
public record BendingSelectPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BendingSelectPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(AvatarUniverseMod.MODID, "bending_select"));

    public static final StreamCodec<FriendlyByteBuf, BendingSelectPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, BendingSelectPayload::pos, BendingSelectPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BendingSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.level().isClientSide) {
                return;
            }
            if (!Config.ENABLE_BENDING.get()) {
                return;
            }
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled()) {
                return;
            }
            if (!bending.hasElement(BendingElement.WATER)) {
                player.displayClientMessage(Component.literal("You are not a waterbender."), true);
                return;
            }
            BlockPos pos = payload.pos();
            if (player.blockPosition().distSqr(pos) > 64.0) {
                return;
            }
            if (!BendingSources.isWaterSource(player.serverLevel(), pos)) {
                return;
            }
            String peekBound = bending.boundAbility(player.getInventory().selected + 1);
            boolean wantsSelect = WaterManipulation.ID.equalsIgnoreCase(peekBound)
                    || Torrent.ID.equalsIgnoreCase(peekBound)
                    || WaterSpout.ID.equalsIgnoreCase(peekBound);
            if (!wantsSelect) {
                // Tap-select is only for abilities that cast FROM a source.
                // Any other bind (arms subs, WaterArms, ...): this tap is a
                // cast through the normal dispatch, not a selection.
                BendingEvents.tryCast(player);
                return;
            }
            bending.selectSource(pos, player.level().getGameTime());
            player.displayClientMessage(Component.literal("Water source selected."), true);
            // Single-click Torrent flow: tapping with Torrent bound readies a
            // WAITING instance, so sneak-hold alone starts forming.
            String bound = bending.boundAbility(player.getInventory().selected + 1);
            if (Torrent.ID.equalsIgnoreCase(bound)
                    && BendingManager.find(player.getUUID(), Torrent.class) == null
                    && !bending.isOnCooldown(Torrent.ID, player.level().getGameTime())) {
                bending.setCooldown(
                        Torrent.ID, player.level().getGameTime() + Config.msToTicks(Config.TORRENT_COOLDOWN_MS.get()));
                BendingManager.start(new Torrent(player, pos));
                player.displayClientMessage(Component.literal("Torrent ready - sneak to form."), true);
            }
            if (WaterSpout.ID.equalsIgnoreCase(bound)
                    && BendingManager.find(player.getUUID(), WaterSpout.class) == null
                    && BendingManager.find(player.getUUID(), WaterSpoutWave.class) == null
                    && !bending.isOnCooldown(WaterSpout.ID, player.level().getGameTime())) {
                // Tap carries its own origin (eye-ray reach differs), so the
                // wave starts even where create() would miss.
                WaterSpoutWave wave = WaterSpoutWave.createAt(player, pos);
                if (wave != null) {
                    BendingManager.start(wave);
                }
            }
        });
    }
}
