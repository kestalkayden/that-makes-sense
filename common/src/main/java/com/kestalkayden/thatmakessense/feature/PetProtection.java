package com.kestalkayden.thatmakessense.feature;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * Pet damage immunity. A tamed pet takes no damage from its owner or from another of that owner's
 * tamed pets - so sweeping attacks, stray arrows, splash potions, and pet infighting can't hurt your
 * animals. Shares the loader damage hooks with {@link NoBerryDamage} / {@link NoEnderPearlDamage}:
 * each loader's handler cancels the hit when this returns true. Only fires when the victim is a tamed
 * pet and the damage traces back to the same owner; mobs, environment, and other players hurt pets
 * normally. Full immunity - cancelling the hit also removes its knockback.
 */
public final class PetProtection {

    private PetProtection() {}

    public static boolean shouldCancel(DamageSource source, LivingEntity victim) {
        if (!ModConfig.get().petProtection.enabled) {
            return false;
        }
        if (!(victim instanceof TamableAnimal pet) || !pet.isTame()) {
            return false;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return false;
        }
        // The owner attacked their own pet.
        if (pet.isOwnedBy(attacker)) {
            return true;
        }
        // Another of the same owner's tamed pets attacked it.
        if (attacker instanceof TamableAnimal otherPet && otherPet.isTame()) {
            LivingEntity otherOwner = otherPet.getOwner();
            return otherOwner != null && pet.isOwnedBy(otherOwner);
        }
        return false;
    }
}
