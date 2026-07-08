package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.item.trading.MerchantOffer;

/**
 * More villager stock. A trade locks when {@code uses >= maxUses}; multiplying the effective threshold
 * lets a villager sell each trade several times as often before running out, while still restocking
 * normally. We deliberately leave {@code maxUses} itself untouched (it is final and flows through
 * copy()/serialization, so scaling it there would compound), and instead override the two methods that
 * consult it: {@link MerchantOffer#isOutOfStock()} and {@link MerchantOffer#setToOutOfStock()}. Keeping
 * both in lockstep means the client (which reconstructs the offer from the synced "exhausted" flag)
 * agrees with the server. Server-authoritative: the server's multiplier governs actual stock.
 */
@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin {

    @Shadow
    private int uses;

    @Shadow
    @Final
    private int maxUses;

    private static int thatmakessense$multiplier() {
        ModConfig.VillagerStock cfg = ModConfig.get().villagerStock;
        return cfg.enabled ? Math.max(1, cfg.multiplier) : 1;
    }

    @Inject(method = "isOutOfStock", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$moreStock(CallbackInfoReturnable<Boolean> cir) {
        int mult = thatmakessense$multiplier();
        if (mult > 1) {
            cir.setReturnValue(this.uses >= this.maxUses * mult);
        }
    }

    @Inject(method = "setToOutOfStock", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$setMoreStock(CallbackInfo ci) {
        int mult = thatmakessense$multiplier();
        if (mult > 1) {
            this.uses = this.maxUses * mult;
            ci.cancel();
        }
    }
}
