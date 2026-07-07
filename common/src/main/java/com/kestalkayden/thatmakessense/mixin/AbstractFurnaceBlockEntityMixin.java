package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * Makes the Cobblestone -&gt; Blackstone blast-furnace recipe a server-side toggle. The recipe itself
 * always ships in the data pack; this gates whether a furnace will actually honour it.
 *
 * <p>In 26.2 the recipe is resolved and assembled in {@code serverTick}, and {@code canBurn} just
 * checks whether the assembled result fits the output slot - it receives {@code (items, maxStackSize,
 * burnResult)}. We identify our recipe by its shape (cobblestone in slot 0, blackstone as the result)
 * and refuse it when the feature is off. This runs server-side, so the server's config is
 * authoritative: a server owner can disable it and connected clients honour that.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Inject(method = "canBurn", at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$gateBlackstone(
            NonNullList<ItemStack> items, int maxStackSize, ItemStack burnResult,
            CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.get().blackstoneRecipe.enabled) {
            return;
        }
        if (!items.isEmpty()
                && items.get(0).is(Items.COBBLESTONE)
                && burnResult.is(Items.BLACKSTONE)) {
            cir.setReturnValue(false);
        }
    }
}
