package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Enchantment compatibility tweaks. Vanilla decides whether two enchantments can share an item in
 * {@link Enchantment#areCompatible} - false when either lists the other in its {@code exclusive_set}.
 * This forces {@code true} for a couple of restrictions people routinely wish weren't there:
 *
 * <ul>
 *   <li><b>Infinity + Mending</b> - both on a bow (they're mutually exclusive via
 *       {@code #minecraft:exclusive_set/bow}).</li>
 *   <li><b>Stack damage enchantments</b> - any two members of {@code #minecraft:exclusive_set/damage}
 *       (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach) on one weapon.</li>
 * </ul>
 *
 * <p>Each is config-gated and read at check time, so toggling is live (affects the next anvil /
 * enchanting-table combine). Only ever relaxes a restriction - it never makes vanilla-compatible
 * enchantments incompatible.
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Unique
    private static final TagKey<Enchantment> THATMAKESSENSE$DAMAGE =
            TagKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace("exclusive_set/damage"));

    @Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$relaxCompatibility(
            Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
        if (first.equals(second)) {
            return; // same enchantment: leave vanilla's false (level-merging is handled elsewhere)
        }

        ModConfig.EnchantmentTweaks cfg = ModConfig.get().enchantmentTweaks;

        if (cfg.infinityMending
                && ((first.is(Enchantments.INFINITY) && second.is(Enchantments.MENDING))
                 || (first.is(Enchantments.MENDING) && second.is(Enchantments.INFINITY)))) {
            cir.setReturnValue(true);
            return;
        }

        if (cfg.stackDamage && first.is(THATMAKESSENSE$DAMAGE) && second.is(THATMAKESSENSE$DAMAGE)) {
            cir.setReturnValue(true);
        }
    }
}
