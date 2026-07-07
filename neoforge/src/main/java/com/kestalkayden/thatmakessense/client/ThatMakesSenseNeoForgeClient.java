package com.kestalkayden.thatmakessense.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only NeoForge wiring. Registers the "Config" button on the Mods list so it opens the shared
 * {@link ConfigScreen}. Reached only via a {@code Dist.CLIENT}-guarded {@code invokestatic} from
 * {@link com.kestalkayden.thatmakessense.ThatMakesSenseNeoForge}, so the dedicated server never
 * verifies this class or the {@code Screen} it references.
 */
public final class ThatMakesSenseNeoForgeClient {

    private ThatMakesSenseNeoForgeClient() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (mod, parent) -> new ConfigScreen(parent));
    }
}
