package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * Makes the Cobblestone -&gt; Blackstone blast-furnace recipe a server-side toggle. The recipe itself
 * always ships in the data pack; this gates whether a furnace will actually honour it, keyed off the
 * config at smelt time.
 *
 * <p>Injects the private static {@code canBurn} - the single check {@code serverTick} uses both to
 * light and to keep smelting - and refuses when the matched recipe is ours and the feature is off.
 * Because this runs server-side, the server's {@code thatmakessense.json} is authoritative: a server
 * owner can disable it and connected clients honour that (nothing smelts, so nothing syncs). The
 * recipe may still appear in the blast furnace's recipe book when disabled, but it won't produce.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Unique
    private static final ResourceKey<Recipe<?>> THATMAKESSENSE$BLACKSTONE_RECIPE =
            ResourceKey.create(Registries.RECIPE,
                    Identifier.fromNamespaceAndPath("thatmakessense", "blackstone_from_blasting_cobblestone"));

    @Inject(
        method = "canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/NonNullList;I)Z",
        at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$gateBlackstone(
            RegistryAccess registryAccess, RecipeHolder<? extends AbstractCookingRecipe> recipe,
            SingleRecipeInput input, NonNullList<ItemStack> items, int maxStackSize,
            CallbackInfoReturnable<Boolean> cir) {
        if (recipe == null || ModConfig.get().blackstoneRecipe.enabled) {
            return;
        }
        if (recipe.id().equals(THATMAKESSENSE$BLACKSTONE_RECIPE)) {
            cir.setReturnValue(false);
        }
    }
}
