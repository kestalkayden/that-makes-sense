package com.kestalkayden.thatmakessense.client;

import com.kestalkayden.thatmakessense.feature.CopperChestMenus;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Client init - binds the enlarged double copper chest menu type to {@link CopperDoubleChestScreen}.
 * Registered under the {@code "client"} entrypoint in {@code fabric.mod.json}; runs after the main
 * initializer, so {@link CopperChestMenus#doubleMenu} is already set.
 */
public class ThatMakesSenseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(CopperChestMenus.doubleMenu, CopperDoubleChestScreen::new);
    }
}
