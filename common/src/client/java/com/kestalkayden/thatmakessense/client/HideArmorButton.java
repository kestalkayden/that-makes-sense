package com.kestalkayden.thatmakessense.client;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Inventory toggle for the Hide Armor feature. A small vanilla-styled button showing a helmet icon;
 * a red slash is drawn over it while armor is hidden. Clicking flips {@code hideArmor.hidden} and
 * persists it. Lives in {@code :common/src/client}, added to the inventory by
 * {@code InventoryScreenMixin}, and only ever class-loaded on the client.
 */
public class HideArmorButton extends AbstractButton {

    /** Square icon button, a touch larger than the 16px item so the sprite has a border. */
    public static final int SIZE = 18;

    private static final ItemStack HELMET_ICON = new ItemStack(Items.IRON_HELMET);
    private static final int SLASH_COLOR = 0xFFE23030;

    public HideArmorButton(int x, int y) {
        super(x, y, SIZE, SIZE, Component.translatable("thatmakessense.hideArmor.button"));
        refreshTooltip();
    }

    @Override
    public void onPress() {
        ModConfig cfg = ModConfig.get();
        cfg.hideArmor.hidden = !cfg.hideArmor.hidden;
        ModConfig.save();
        refreshTooltip();
    }

    /** Tooltip names the action the next click performs, given the current state. */
    private void refreshTooltip() {
        String key = ModConfig.get().hideArmor.hidden
                ? "thatmakessense.hideArmor.tooltip.show"
                : "thatmakessense.hideArmor.tooltip.hide";
        setTooltip(Tooltip.create(Component.translatable(key)));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draws the default vanilla button sprite background; renderString() is overridden below to a
        // no-op so the translatable message (kept for tooltip/narration) never paints text over the icon.
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        int iconX = getX() + (getWidth() - 16) / 2;
        int iconY = getY() + (getHeight() - 16) / 2;
        guiGraphics.renderItem(HELMET_ICON, iconX, iconY);
        if (ModConfig.get().hideArmor.hidden) {
            for (int i = 0; i < 16; i++) {
                guiGraphics.fill(iconX + i, iconY + i, iconX + i + 2, iconY + i + 2, SLASH_COLOR);
            }
        }
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        // Icon-only button: suppress the default AbstractButton label text.
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
