package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Stackable Totems of Undying.
 *
 * <p>{@link ItemInstance#getMaxStackSize()} is the single choke point every inventory-merge path
 * consults (pickup, slot merge, hopper transfer). A Totem of Undying registers with a
 * {@code MAX_STACK_SIZE} component of 1, so the default reads back 1; we override the result to a
 * full stack for totems when the feature is on. Non-totems fall through untouched, so this never
 * re-implements the component lookup.
 *
 * <p>This is a common (both-sides) mixin: stacking is resolved on the server, which is authoritative,
 * so a server's config decides whether totems stack for everyone connected to it.
 */
@Mixin(ItemInstance.class)
public interface ItemInstanceMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$stackableTotems(CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.get().stackableTotems.enabled
                && (Object) this instanceof ItemStack stack
                && stack.is(Items.TOTEM_OF_UNDYING)) {
            cir.setReturnValue(64);
        }
    }
}
