package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.entity.Leashable;

/**
 * Longer leads. A lead snaps once the leashed entity gets past {@code leashSnapDistance()} - vanilla
 * 12 blocks. Raising that default lets mobs wander further before it breaks; the elastic pull-back
 * ({@code leashElasticDistance()}, 6 blocks) is untouched, so it's simply more rope. Mobs that
 * override the snap distance (e.g. happy ghasts) keep their own. Leash logic runs server-side.
 */
@Mixin(Leashable.class)
public interface LeashableMixin {

    @Inject(method = "leashSnapDistance", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$longerLeads(CallbackInfoReturnable<Double> cir) {
        ModConfig.LongerLeads cfg = ModConfig.get().longerLeads;
        if (cfg.enabled) {
            // Never shorter than vanilla, so a stray small config value can't make leads worse.
            cir.setReturnValue(Math.max(cfg.distance, 12.0));
        }
    }
}
