package com.kestalkayden.thatmakessense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(ThatMakesSenseNeoForge.MOD_ID)
public class ThatMakesSenseNeoForge {

    public static final String MOD_ID = "thatmakessense";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ThatMakesSenseNeoForge(ModContainer container, IEventBus modBus) {
        LOGGER.info("Initializing That Makes Sense (NeoForge)");
        ModConfig.load(FMLPaths.CONFIGDIR.get());

        // No Crop Trample: cancel the vanilla trample so farmland keeps its crops.
        NeoForge.EVENT_BUS.addListener(ThatMakesSenseNeoForge::onFarmlandTrample);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            // Config screen wiring lives in a client-only class reached solely through this guarded
            // invokestatic, so a dedicated server never links the Screen-referencing lambda.
            ThatMakesSenseNeoForgeClient.register(container);
        }
    }

    private static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (ModConfig.get().shouldPreventTrampling(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
