package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

/**
 * No Ender Pearl Damage. 1.21.1 deals the flat teleport damage via {@code Entity.hurt(DamageSource,
 * float)} using the exact same generic {@code DamageSources.fall()} source that real fall damage uses
 * (there is nothing on the {@code DamageSource} itself to tell the two apart). 1.21.8 both split
 * {@code hurt} into server/client halves ({@code onHit} now calls {@code ServerPlayer.hurtServer(
 * ServerLevel, DamageSource, float)}) and gained a dedicated {@code DamageSources.enderPearl()} source
 * - but we still wrap the specific call site rather than switch to a damage-type check, so this stays
 * a minimal, behavior-preserving port. Skipping that wrapped call when the feature is enabled leaves
 * real fall damage, dealt through a completely different code path, untouched.
 */
@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @WrapOperation(
        method = "onHit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;hurtServer("
                   + "Lnet/minecraft/server/level/ServerLevel;"
                   + "Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean thatmakessense$noEnderPearlDamage(
            ServerPlayer player, ServerLevel level, DamageSource source, float amount, Operation<Boolean> original) {
        if (ModConfig.get().noEnderPearlDamage.enabled) {
            return false;
        }
        return original.call(player, level, source, amount);
    }
}
