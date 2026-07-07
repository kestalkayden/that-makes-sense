package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

/**
 * No Enderman Griefing (pickup half). Disables the AI goal that lets an enderman lift a block out of
 * the world, so terrain is left alone. Targeted by name because the goal is a private inner class of
 * {@code EnderMan}. Paired with {@code EndermanLeaveBlockGoalMixin} (placement half).
 */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public abstract class EndermanTakeBlockGoalMixin {

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$noGrief(CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.get().noEndermanGriefing.enabled) {
            cir.setReturnValue(false);
        }
    }
}
