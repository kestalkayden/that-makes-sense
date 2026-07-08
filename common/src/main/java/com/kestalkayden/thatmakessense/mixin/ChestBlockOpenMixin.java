package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.level.block.ChestBlock;

/**
 * Chests open under blocks. Vanilla refuses to open a chest when a solid (redstone-conductor) block
 * sits directly above it, so the lid has room to swing. {@code isBlockedChestByBlock} is the
 * block-above half of {@code isChestBlockedAt}; forcing it false lets chests open regardless of what's
 * on top. The cat-on-chest half is left alone, so a sitting cat still blocks the chest as in vanilla.
 */
@Mixin(ChestBlock.class)
public abstract class ChestBlockOpenMixin {

    @Inject(method = "isBlockedChestByBlock", at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$openUnderBlocks(CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.get().chestsUnderBlocks.enabled) {
            cir.setReturnValue(false);
        }
    }
}
