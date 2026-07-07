package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

/**
 * No Enderman Griefing (placement half). Disables the AI goal that lets an enderman set a carried
 * block down into the world. Targeted by name (private inner class of {@code EnderMan}). Paired with
 * {@code EndermanTakeBlockGoalMixin} (pickup half). An enderman already carrying a block simply keeps
 * holding it.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanLeaveBlockGoal")
public abstract class EndermanLeaveBlockGoalMixin {

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$noGrief(CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.get().noEndermanGriefing.enabled) {
            cir.setReturnValue(false);
        }
    }
}
