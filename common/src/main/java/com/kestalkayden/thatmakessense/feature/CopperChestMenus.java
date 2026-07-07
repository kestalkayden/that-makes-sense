package com.kestalkayden.thatmakessense.feature;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Holder + factories for the custom 8-row menu used by enlarged double copper chests.
 *
 * <p>Vanilla's tallest chest menu is {@code GENERIC_9x6} (54 slots); a double copper chest with two
 * 4-row halves needs 72 slots / 8 rows, which has no vanilla menu type. Each loader registers the
 * type its own way (Fabric: {@code new MenuType<>} + {@code Registry.register}; NeoForge:
 * {@code IMenuTypeExtension.create} + {@code DeferredRegister}) and assigns it to {@link #doubleMenu}.
 * The shared {@code ChestBlockMixin} (server) and each loader's client menu supplier build a plain
 * vanilla {@link ChestMenu} at 8 rows through the factories below - its slot layout lines up exactly
 * with the tiled background drawn by {@code CopperDoubleChestScreen}.
 */
public final class CopperChestMenus {

    /** Rows / slots for a double copper chest (two enlarged 4-row halves). */
    public static final int ROWS = 8;
    public static final int SLOTS = ROWS * 9;

    /** The registered 8-row menu type, assigned by each loader during mod init. */
    public static MenuType<ChestMenu> doubleMenu;

    private CopperChestMenus() {
    }

    /** Client-side reconstruction from the network: a standalone 72-slot container to sync into. */
    public static ChestMenu createClientMenu(int syncId, Inventory inv) {
        return new ChestMenu(doubleMenu, syncId, inv, new SimpleContainer(SLOTS), ROWS);
    }

    /** Server-side: back the menu with the real combined container (both chest halves). */
    public static ChestMenu createServerMenu(int syncId, Inventory inv, Container container) {
        return new ChestMenu(doubleMenu, syncId, inv, container, ROWS);
    }
}
