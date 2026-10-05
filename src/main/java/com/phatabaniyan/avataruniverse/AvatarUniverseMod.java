package com.phatabaniyan.avataruniverse;

import com.mojang.logging.LogUtils;
import com.phatabaniyan.avataruniverse.bending.BendingNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AvatarUniverseMod.MODID)
public class AvatarUniverseMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "avataruniverse";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AvatarUniverseMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Custom packets (mod-bus lifecycle event, not gameplay).
        modEventBus.addListener(BendingNetworking::onRegisterPayloads);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Common setup code (networking, capabilities, etc. go here)
        LOGGER.info("AvatarUniverse common setup complete");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (Config.ENABLE_DEBUG_LOGGING.getAsBoolean()) {
            LOGGER.info("AvatarUniverse server starting");
        }
    }
}
