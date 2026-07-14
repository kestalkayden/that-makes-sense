package com.kestalkayden.thatmakessense.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client init entrypoint. Registered under the {@code "client"} entrypoint in
 * {@code fabric.mod.json}; currently a no-op placeholder (the Config screen is opened directly by
 * {@link ModMenuIntegration}, and Hide Armor's button is added by {@code InventoryScreenMixin}).
 * Kept so the entrypoint declaration doesn't need touching if a future feature needs client init.
 */
public class ThatMakesSenseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
    }
}
