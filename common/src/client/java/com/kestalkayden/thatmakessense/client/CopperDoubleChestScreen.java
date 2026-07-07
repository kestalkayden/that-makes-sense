package com.kestalkayden.thatmakessense.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/**
 * Screen for the enlarged double copper chest (8 rows / 72 slots). Vanilla's {@code ContainerScreen}
 * tops out at 6 rows because it reads the slot rows straight from {@code generic_54.png}, which only
 * has six. This tiles the vanilla texture instead - title strip, N slot-row strips (reusing the sixth
 * row's slice for rows beyond it), then the player-inventory panel - so no new texture asset is needed.
 *
 * <p>A plain vanilla {@link ChestMenu} at 8 rows lays its slots out on the standard 176-wide grid, so
 * the tiled background lines up exactly. Registered per loader (Fabric {@code MenuScreens.register},
 * NeoForge {@code RegisterMenuScreensEvent}) against the shared 8-row menu type.
 */
public class CopperDoubleChestScreen extends AbstractContainerScreen<ChestMenu> {

    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    private static final int TITLE_HEIGHT = 17;
    private static final int ROW_HEIGHT = 18;
    private static final int PLAYER_PANEL_HEIGHT = 96;
    private static final float PLAYER_PANEL_V = 126.0F;
    private static final int VANILLA_CHEST_ROWS = 6;
    private static final int TEXTURE_SIZE = 256;

    private static final int PANEL_WIDTH = 176;

    private final int rows;

    public CopperDoubleChestScreen(ChestMenu menu, Inventory inventory, Component title) {
        // imageWidth/imageHeight are final in 26.2 - they must be passed through the constructor.
        super(menu, inventory, title, PANEL_WIDTH, 114 + menu.getRowCount() * ROW_HEIGHT);
        this.rows = menu.getRowCount();
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        // Title / top border.
        blit(graphics, x, y, this.imageWidth, TITLE_HEIGHT, 0.0F);

        // Slot rows: rows past the sixth reuse the sixth row's slice (all rows are identical cells).
        for (int row = 0; row < this.rows; row++) {
            float v = TITLE_HEIGHT + Math.min(row, VANILLA_CHEST_ROWS - 1) * ROW_HEIGHT;
            blit(graphics, x, y + TITLE_HEIGHT + row * ROW_HEIGHT, this.imageWidth, ROW_HEIGHT, v);
        }

        // Player inventory panel.
        blit(graphics, x, y + TITLE_HEIGHT + this.rows * ROW_HEIGHT, this.imageWidth, PLAYER_PANEL_HEIGHT, PLAYER_PANEL_V);
    }

    private void blit(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float v) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0.0F, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
