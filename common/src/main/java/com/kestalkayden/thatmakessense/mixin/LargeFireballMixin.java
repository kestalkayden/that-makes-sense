package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;

/**
 * No Ghast Fireball Terrain Damage. A ghast's fireball still hurts entities and its direct-hit damage
 * is untouched, but the blast no longer breaks blocks.
 *
 * <p>{@code LargeFireball.onHit} calls {@code Level.explode(..., ExplosionInteraction.MOB)}; the
 * interaction argument governs block destruction (and fire), not entity damage. Swapping it to
 * {@code NONE} spares terrain while keeping the explosion's effect on entities - the same lever used
 * for No Creeper Block Damage.
 */
@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin {

    @ModifyArg(
        method = "onHit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode("
                   + "Lnet/minecraft/world/entity/Entity;DDDFZ"
                   + "Lnet/minecraft/world/level/Level$ExplosionInteraction;)"
                   + "Lnet/minecraft/world/level/Explosion;"),
        index = 6)
    private Level.ExplosionInteraction thatmakessense$noGhastFireballBlockDamage(Level.ExplosionInteraction original) {
        return ModConfig.get().noGhastFireballDamage.enabled ? Level.ExplosionInteraction.NONE : original;
    }
}
