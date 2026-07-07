package com.kestalkayden.thatmakessense.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu integration - supplies the "Config" button on the Fabric mod list screen.
 *
 * <p>Client-only (references {@link ConfigScreen}, which extends {@code Screen}); registered under
 * the {@code "modmenu"} entrypoint in {@code fabric.mod.json}.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }
}
