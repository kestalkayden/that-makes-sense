package com.kestalkayden.thatmakessense.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.inventory.AnvilMenu;

/**
 * Removes the anvil "Too Expensive!" cap. Vanilla blanks the result whenever the level cost reaches 40
 * in survival - {@code AnvilMenu.createResult}: {@code cost >= 40 && !player.getAbilities().instabuild}.
 * (1.21.1 reads the {@code instabuild} field directly rather than calling
 * {@code Player.hasInfiniteMaterials()}, which this method never calls at all.) {@code instabuild} is
 * also read once earlier in the same method for an unrelated enchantment-compatibility check, so this
 * targets the field read by {@code ordinal = 1} (the second occurrence) to hit only the cost-cap site.
 * Making that read true when enabled skips the blanking, so pricey combines and repairs still produce a
 * result. You still need the levels to take it - the result slot's own affordability check
 * ({@code mayPickup}) is untouched, so this isn't free. Server-authoritative; the client mirrors it for
 * display.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @ModifyExpressionValue(
        method = "createResult",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z",
            ordinal = 1))
    private boolean thatmakessense$noAnvilCap(boolean original) {
        return original || ModConfig.get().noAnvilCap.enabled;
    }
}
