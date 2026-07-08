package com.kestalkayden.thatmakessense.client;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
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
    public void onPress(InputWithModifiers input) {
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
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractDefaultSprite(graphics);
        int iconX = getX() + (getWidth() - 16) / 2;
        int iconY = getY() + (getHeight() - 16) / 2;
        graphics.item(HELMET_ICON, iconX, iconY);
        if (ModConfig.get().hideArmor.hidden) {
            for (int i = 0; i < 16; i++) {
                graphics.fill(iconX + i, iconY + i, iconX + i + 2, iconY + i + 2, SLASH_COLOR);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
