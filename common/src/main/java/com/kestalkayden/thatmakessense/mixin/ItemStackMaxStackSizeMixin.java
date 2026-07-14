package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Stackable Totems of Undying.
 *
 * <p>1.21.1 has no {@code ItemInstance} indirection: {@link ItemStack#getMaxStackSize()} reads the
 * {@code MAX_STACK_SIZE} data component straight off the stack itself (it does not consult
 * {@code Item.getDefaultMaxStackSize()} at all), so this is the single choke point every
 * inventory-merge path consults (pickup, slot merge, hopper transfer). A Totem of Undying carries a
 * {@code MAX_STACK_SIZE} component of 1, so the default reads back 1; we override the result to a
 * full stack for totems when the feature is on. Non-totems fall through untouched, so this never
 * re-implements the component lookup.
 *
 * <p>This is a common (both-sides) mixin: stacking is resolved on the server, which is authoritative,
 * so a server's config decides whether totems stack for everyone connected to it.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMaxStackSizeMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$stackableTotems(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (ModConfig.get().stackableTotems.enabled && self.is(Items.TOTEM_OF_UNDYING)) {
            cir.setReturnValue(64);
        }
    }
}
