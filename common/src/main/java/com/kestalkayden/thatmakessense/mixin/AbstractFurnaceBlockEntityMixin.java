package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.kestalkayden.thatmakessense.feature.TmsItems;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * Server-side toggles for this mod's furnace/smoker/blast recipes. Each recipe always ships in the
 * data pack; this gates whether a furnace will honour it, keyed on config.
 *
 * <p>In 26.2 the recipe is resolved and assembled in {@code serverTick}, and {@code canBurn} just
 * checks whether the assembled result fits the output slot - it receives {@code (items, maxStackSize,
 * burnResult)}. We identify each of our recipes by its shape (input in slot 0 + assembled result) and
 * refuse it when its feature is off. Runs server-side, so a server's config is authoritative. Because
 * each recipe type binds to one appliance (smelting = furnace, smoking = smoker, blasting = blast
 * furnace) and vanilla has no rotten-flesh recipe, the result item alone disambiguates them.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Inject(method = "canBurn", at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$gateFurnaceRecipes(
            NonNullList<ItemStack> items, int maxStackSize, ItemStack burnResult,
            CallbackInfoReturnable<Boolean> cir) {
        if (items.isEmpty()) {
            return;
        }
        ItemStack input = items.get(0);
        ModConfig cfg = ModConfig.get();

        // Cobblestone -> Blackstone (blast furnace).
        if (!cfg.blackstoneRecipe.enabled && input.is(Items.COBBLESTONE) && burnResult.is(Items.BLACKSTONE)) {
            cir.setReturnValue(false);
            return;
        }
        // Rotten Flesh -> Leather (furnace).
        if (!cfg.rottenFleshLeather.enabled && input.is(Items.ROTTEN_FLESH) && burnResult.is(Items.LEATHER)) {
            cir.setReturnValue(false);
            return;
        }
        // Rotten Flesh -> Zombie Jerky (smoker).
        if (!cfg.zombieJerky.enabled && input.is(Items.ROTTEN_FLESH)
                && TmsItems.zombieJerky != null && burnResult.is(TmsItems.zombieJerky)) {
            cir.setReturnValue(false);
        }
    }
}
