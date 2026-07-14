package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.kestalkayden.thatmakessense.feature.TmsItems;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * Server-side toggles for this mod's furnace/smoker/blast recipes. Each recipe always ships in the
 * data pack; this gates whether a furnace will honour it, keyed on config.
 *
 * <p>1.21.1's {@code canBurn} takes {@code (RegistryAccess, RecipeHolder<?>, NonNullList<ItemStack>
 * items, int maxStackSize)} on Fabric - the assembled output is no longer a direct parameter, so we
 * resolve it ourselves via {@code recipe.value().getResultItem(registryAccess)}. NeoForge patches an
 * extra trailing {@code AbstractFurnaceBlockEntity} parameter onto {@code canBurn}, so the handler
 * can't declare a fixed prefix of the target's own parameters and stay valid on both loaders. Instead
 * it captures only the three locals it actually needs via MixinExtras {@code @Local(argsOnly = true)}
 * (matched by type - each type appears exactly once among {@code canBurn}'s arguments on both
 * loaders), so this single mixin applies unmodified regardless of which loader patched the method.
 * We identify each of our recipes by its shape (input in slot 0 + resolved result) and refuse it when
 * its feature is off. Runs server-side, so a server's config is authoritative. Because each recipe
 * type binds to one appliance (smelting = furnace, smoking = smoker, blasting = blast furnace) and
 * vanilla has no rotten-flesh recipe, the result item alone disambiguates them.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Inject(method = "canBurn", at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$gateFurnaceRecipes(
            CallbackInfoReturnable<Boolean> cir,
            @Local(argsOnly = true) RegistryAccess registryAccess,
            @Local(argsOnly = true) RecipeHolder<?> recipe,
            @Local(argsOnly = true) NonNullList<ItemStack> items) {
        if (recipe == null || items.isEmpty()) {
            return;
        }
        ItemStack input = items.get(0);
        ItemStack burnResult = recipe.value().getResultItem(registryAccess);
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
