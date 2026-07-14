package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

/**
 * No Ender Pearl Damage. 1.21.1 has no dedicated "ender pearl" damage type - {@code onHit} deals the
 * flat 5 hearts of teleport damage with the exact same generic {@code DamageSources.fall()} source
 * that real fall damage uses (there is nothing on the {@code DamageSource} itself to tell the two
 * apart). So instead of a generic damage-type check, this wraps the specific
 * {@code Entity.hurt(DamageSource, float)} call inside {@link ThrownEnderpearl#onHit} and skips it
 * when the feature is enabled - real fall damage, dealt through a completely different code path, is
 * untouched.
 */
@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @WrapOperation(
        method = "onHit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean thatmakessense$noEnderPearlDamage(
            Entity entity, DamageSource source, float amount, Operation<Boolean> original) {
        if (ModConfig.get().noEnderPearlDamage.enabled) {
            return false;
        }
        return original.call(entity, source, amount);
    }
}
