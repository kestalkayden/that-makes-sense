package com.kestalkayden.thatmakessense.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.inventory.AnvilMenu;

/**
 * Removes the anvil "Too Expensive!" cap (Fabric). Vanilla blanks the result whenever the level cost
 * reaches 40 in survival - {@code AnvilMenu.createResult}: {@code cost >= 40 &&
 * !player.hasInfiniteMaterials()}. (1.21.1 read the {@code instabuild} field directly instead; 1.21.8
 * routes it through {@code Player.hasInfiniteMaterials()}.) That call also appears once earlier in the
 * same method for an unrelated enchantment-compatibility check, so this targets the invocation by
 * {@code ordinal = 1} (the second occurrence) to hit only the cost-cap site. Making that call read true
 * when enabled skips the blanking, so pricey combines and repairs still produce a result. You still
 * need the levels to take it - the result slot's own affordability check ({@code mayPickup}) is
 * untouched, so this isn't free. Server-authoritative; the client mirrors it for display.
 *
 * <p>This is Fabric-specific (not in :common) because NeoForge's own patches split {@code
 * createResult()} into a thin wrapper plus a {@code createResultInternal()} that holds the real logic
 * - and there, the enchantment-compatibility check was reverted to the raw {@code instabuild} field, so
 * only a single {@code hasInfiniteMaterials()} call remains. See the NeoForge module's own
 * {@code AnvilMenuMixin} for that loader's equivalent.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @ModifyExpressionValue(
        method = "createResult",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z",
            ordinal = 1))
    private boolean thatmakessense$noAnvilCap(boolean original) {
        return original || ModConfig.get().noAnvilCap.enabled;
    }
}
