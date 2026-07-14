package com.kestalkayden.thatmakessense.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.inventory.AnvilMenu;

/**
 * Removes the anvil "Too Expensive!" cap (NeoForge). NeoForge patches {@code AnvilMenu} so the
 * overridable {@code createResult()} is just a thin wrapper that calls {@code createResultInternal()}
 * (the vanilla logic) and then fires {@code AnvilUpdateEvent} - the real
 * {@code cost >= 40 && !player.hasInfiniteMaterials()} cap check lives in {@code createResultInternal}.
 * NeoForge also reverted the earlier enchantment-compatibility check back to reading
 * {@code player.getAbilities().instabuild} directly rather than {@code hasInfiniteMaterials()}, so
 * unlike Fabric's copy of this mixin, {@code createResultInternal} has only the single cost-cap call -
 * no {@code ordinal} is needed. Making that call read true when enabled skips the blanking, so pricey
 * combines and repairs still produce a result. You still need the levels to take it - the result slot's
 * own affordability check ({@code mayPickup}) is untouched, so this isn't free. Server-authoritative;
 * the client mirrors it for display.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @ModifyExpressionValue(
        method = "createResultInternal",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"))
    private boolean thatmakessense$noAnvilCap(boolean original) {
        return original || ModConfig.get().noAnvilCap.enabled;
    }
}
