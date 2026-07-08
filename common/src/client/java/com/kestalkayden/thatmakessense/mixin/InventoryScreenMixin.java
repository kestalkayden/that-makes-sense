package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kestalkayden.thatmakessense.client.HideArmorButton;
import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Adds the Hide Armor toggle to the survival inventory. The button sits in the gap between the player
 * paperdoll and the crafting grid, near the top. {@code init()} re-runs on open and on resize, so a
 * fresh button is added each time (matching how vanilla rebuilds its widgets).
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @Inject(method = "init", at = @At("TAIL"))
    private void thatmakessense$addHideArmorButton(CallbackInfo ci) {
        if (!ModConfig.get().hideArmor.enabled) {
            return;
        }
        this.addRenderableWidget(new HideArmorButton(this.leftPos + 77, this.topPos + 7));
    }
}
