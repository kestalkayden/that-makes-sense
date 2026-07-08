package com.kestalkayden.thatmakessense.feature;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * No pet fall damage. Tamed pets path badly off ledges and cliffs; this cancels fall damage for any
 * tamed {@link TamableAnimal} (wolves, cats, parrots). Shares the loader damage hooks with
 * {@link NoBerryDamage} / {@link NoEnderPearlDamage} / {@link PetProtection} - each handler cancels the
 * hit when this returns true. Only fall damage, only tamed pets; everything else hurts them normally.
 */
public final class NoPetFallDamage {

    private NoPetFallDamage() {}

    public static boolean shouldCancel(DamageSource source, LivingEntity victim) {
        return ModConfig.get().noPetFallDamage.enabled
                && source.is(DamageTypes.FALL)
                && victim instanceof TamableAnimal pet
                && pet.isTame();
    }
}
