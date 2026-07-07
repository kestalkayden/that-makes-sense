package com.kestalkayden.thatmakessense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ThatMakesSenseFabric implements ModInitializer {

    public static final String MOD_ID = "thatmakessense";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing That Makes Sense (Fabric)");
        ModConfig.load(FabricLoader.getInstance().getConfigDir());
        // No Crop Trample is applied by FarmlandBlockMixin, which consults ModConfig at runtime.
    }
}
