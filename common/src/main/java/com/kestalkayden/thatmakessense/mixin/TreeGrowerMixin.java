package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Mega trees from 2x2 saplings. Vanilla only gives spruce / jungle / dark-oak / pale-oak a 2x2 "mega"
 * form. {@code TreeGrower.growTree} already does all the 2x2 detection + placement generically - it just
 * asks {@code getConfiguredMegaFeature()} for a feature key, which is empty for birch/oak/cherry. We
 * hand back our own registered giant configured feature for those growers when the toggle is on, so a
 * 2x2 of their saplings grows a giant. Single saplings are untouched: with no 2x2 present, growTree
 * falls through to the normal tree, and if our feature is missing it also falls back safely. Server-side.
 */
@Mixin(TreeGrower.class)
public abstract class TreeGrowerMixin {

    @Inject(method = "getConfiguredMegaFeature", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$megaTrees(
            RandomSource random, CallbackInfoReturnable<ResourceKey<ConfiguredFeature<?, ?>>> cir) {
        if (!ModConfig.get().megaTrees.enabled) {
            return;
        }
        TreeGrower self = (TreeGrower) (Object) this;
        String path = null;
        if (self == TreeGrower.BIRCH) {
            path = "mega_birch";
        } else if (self == TreeGrower.OAK) {
            path = "mega_oak";
        } else if (self == TreeGrower.ACACIA) {
            path = "mega_acacia";
        } else if (self == TreeGrower.CHERRY) {
            path = "mega_cherry";
        }
        if (path != null) {
            cir.setReturnValue(ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath("thatmakessense", path)));
        }
    }
}
