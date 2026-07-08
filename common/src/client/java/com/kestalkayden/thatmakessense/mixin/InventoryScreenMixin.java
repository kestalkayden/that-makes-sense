package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kestalkayden.thatmakessense.client.HideArmorButton;
import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Adds the Hide Armor toggle to the survival inventory. The button sits in the gap between the player
 * paperdoll and the crafting grid, near the top. {@code init()} re-runs on open and on resize, so a
 * fresh button is added each time (matching how vanilla rebuilds its widgets).
 *
 * <p>This mixin declares {@link AbstractContainerScreen} (an ancestor of {@link InventoryScreen}) as its
 * superclass so that {@code leftPos}, {@code topPos} and {@code addRenderableWidget} resolve through
 * ordinary inheritance. Mixin only reliably locates inherited members when the mixin extends the class
 * that declares them - an {@code @Shadow} of the inherited generic {@code addRenderableWidget} fails to
 * resolve against the target. The constructor below is never called (the target is already built); it
 * exists solely to satisfy javac's implicit {@code super()} and is discarded when the mixin is applied.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    private InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void thatmakessense$addHideArmorButton(CallbackInfo ci) {
        if (!ModConfig.get().hideArmor.enabled) {
            return;
        }
        this.addRenderableWidget(new HideArmorButton(this.leftPos + 77, this.topPos + 7));
    }
}
