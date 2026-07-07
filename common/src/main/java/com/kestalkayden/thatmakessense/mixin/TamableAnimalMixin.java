package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * No damage from pets teleporting to you. When a tamed animal teleports to its owner it snaps to a
 * new position but never clears its fall distance (unlike an ender pearl teleport, which does), so a
 * pet that was falling can land and take fall damage right after arriving. Resetting fall distance at
 * the end of the teleport attempt mirrors the ender-pearl behaviour and spares the pet.
 */
@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {

    @Inject(method = "tryToTeleportToOwner", at = @At("TAIL"))
    private void thatmakessense$noTeleportFallDamage(CallbackInfo ci) {
        if (ModConfig.get().noPetTeleportDamage.enabled) {
            ((Entity) (Object) this).resetFallDistance();
        }
    }
}
