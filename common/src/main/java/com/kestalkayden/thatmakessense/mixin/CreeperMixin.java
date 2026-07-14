package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 * No Creeper Block Damage. A creeper's explosion still hurts entities, but leaves terrain intact.
 *
 * <p>{@code Creeper.explodeCreeper} calls {@code Level.explode(..., ExplosionInteraction.MOB)} (the
 * static type at the call site is {@code Level}, not {@code ServerLevel}, even though the receiver is
 * always a {@code ServerLevel} at runtime; the 1.21.1 overload also returns {@code Explosion}, not
 * {@code void}); the interaction argument controls block destruction and fire, not entity damage.
 * Swapping it to {@code NONE} keeps the blast (and its knockback/damage) while sparing blocks. Covers
 * charged creepers too - they share this method with a larger radius.
 */
@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @ModifyArg(
        method = "explodeCreeper",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode("
                   + "Lnet/minecraft/world/entity/Entity;DDDF"
                   + "Lnet/minecraft/world/level/Level$ExplosionInteraction;)"
                   + "Lnet/minecraft/world/level/Explosion;"),
        index = 5)
    private Level.ExplosionInteraction thatmakessense$noCreeperBlockDamage(Level.ExplosionInteraction original) {
        return ModConfig.get().noCreeperBlockDamage.enabled ? Level.ExplosionInteraction.NONE : original;
    }
}
