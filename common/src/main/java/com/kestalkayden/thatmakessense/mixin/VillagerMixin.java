package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.npc.Villager;

/**
 * Villagers don't turn into witches when struck by lightning. Vanilla's {@code Villager.thunderHit}
 * converts the villager to a witch; cancelling it at HEAD makes the villager shrug off the strike
 * entirely, so a storm can't wipe out your trading hall. (Full lightning immunity for villagers is the
 * simplest, most robust guarantee against the conversion.)
 */
@Mixin(Villager.class)
public abstract class VillagerMixin {

    @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$noWitchConversion(ServerLevel level, LightningBolt lightningBolt, CallbackInfo ci) {
        if (ModConfig.get().noVillagerWitch.enabled) {
            ci.cancel();
        }
    }
}
