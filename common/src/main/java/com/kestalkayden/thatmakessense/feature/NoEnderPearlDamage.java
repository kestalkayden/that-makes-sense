package com.kestalkayden.thatmakessense.feature;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

/**
 * No ender pearl teleport damage. Throwing an ender pearl deals a flat 5 hearts of
 * {@link DamageTypes#ENDER_PEARL} on arrival; this cancels it. Shares the loader damage hooks with
 * {@link NoBerryDamage} - each loader's handler cancels when this returns true.
 */
public final class NoEnderPearlDamage {

    private NoEnderPearlDamage() {}

    public static boolean shouldCancel(DamageSource source) {
        return ModConfig.get().noEnderPearlDamage.enabled && source.is(DamageTypes.ENDER_PEARL);
    }
}
