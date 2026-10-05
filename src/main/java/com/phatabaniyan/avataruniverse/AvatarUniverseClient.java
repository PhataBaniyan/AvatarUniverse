package com.phatabaniyan.avataruniverse;

import com.phatabaniyan.avataruniverse.bending.BendingCastPayload;
import com.phatabaniyan.avataruniverse.bending.BendingSelectPayload;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = AvatarUniverseMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with
// @SubscribeEvent
@EventBusSubscriber(modid = AvatarUniverseMod.MODID, value = Dist.CLIENT)
public class AvatarUniverseClient {
    private static boolean attackWasDown;

    public AvatarUniverseClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AvatarUniverseMod.LOGGER.info("AvatarUniverse client setup complete");
    }

    /**
     * Left-click handling the server can never see on its own (Korra model:
     * plain left-click, no sneak). Left-clicking air sends no vanilla packet,
     * so when the attack key is newly pressed: aiming at water within reach
     * selects it as the bending source ({@code BendingSelectPayload}), aiming
     * at nothing asks the server to cast ({@code BendingCastPayload}).
     * Block/entity aims are left to the server-side interact events to avoid
     * double casts. The server re-validates everything either way.
     */
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean down = minecraft.options.keyAttack.isDown();
        try {
            if (!down
                    || attackWasDown
                    || minecraft.player == null
                    || minecraft.level == null
                    || minecraft.screen != null) {
                return;
            }
            HitResult fluidHit = minecraft.player.pick(8.0, 0.0F, true);
            if (fluidHit instanceof BlockHitResult blockHit
                    && BendingSources.isWaterSource(minecraft.level, blockHit.getBlockPos())) {
                PacketDistributor.sendToServer(new BendingSelectPayload(blockHit.getBlockPos()));
                return;
            }
            if (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.MISS) {
                return;
            }
            PacketDistributor.sendToServer(new BendingCastPayload());
        } finally {
            attackWasDown = down;
        }
    }
}
