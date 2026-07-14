package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Makes the craftable name tag a server-side toggle. The recipe always ships in the data pack; this
 * blanks its result when the feature is off. Vanilla 1.21.1 has no name-tag crafting recipe, so any
 * crafted name tag is ours - identify it by output rather than by recipe id.
 *
 * <p>{@code CraftingMenu.slotChangedCraftingGrid} computes the result and writes it via
 * {@code ResultContainer.setItem(0, result)} on the server; {@code @ModifyArg} swaps a name-tag result
 * for empty when disabled, so the result slot shows nothing and the craft can't complete. This static
 * method is shared by the player's 2x2 grid too, though the recipe itself needs a 3x3 table.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @ModifyArg(
        method = "slotChangedCraftingGrid",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"),
        index = 1)
    private static ItemStack thatmakessense$gateNameTag(ItemStack result) {
        if (!ModConfig.get().craftableNameTag.enabled && result.is(Items.NAME_TAG)) {
            return ItemStack.EMPTY;
        }
        return result;
    }
}
