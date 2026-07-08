package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cobweb harvestable instantly with shears. Vanilla shears mine cobweb at speed 15 (via the tool
 * component) - fast but not instant. Returning a large destroy speed for shears-on-cobweb pushes the
 * per-tick break progress well past 1.0, so the block pops in a single tick and still drops cobweb
 * (shears are a valid harvest tool for it).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    /** Comfortably above cobweb hardness x the harvest divisor, so break progress clears 1.0 in one tick. */
    private static final float THATMAKESSENSE_INSTANT_SPEED = 1000.0F;

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$instantCobweb(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (ModConfig.get().cobwebShears.enabled
                && ((ItemStack) (Object) this).is(Items.SHEARS)
                && state.is(Blocks.COBWEB)) {
            cir.setReturnValue(THATMAKESSENSE_INSTANT_SPEED);
        }
    }
}
