package com.kestalkayden.thatmakessense.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Infinity on all arrow types. Vanilla's Infinity zeroes ammo use only for the plain arrow - its
 * enchantment data conditions the effect on {@code minecraft:arrow} - so tipped and spectral arrows
 * are still consumed. When an Infinity bow fires any arrow, we override the computed ammo cost to 0.
 *
 * <p>No dupe risk: {@code useAmmo}'s {@code ammoToUse == 0} branch already stamps the fired arrow with
 * {@code INTANGIBLE_PROJECTILE}, so it can't be picked back up. This only matters server-side (the
 * cost is computed there; the client already treats a drawn shot as free), and it never makes a shot
 * cost more - it only ever drops a cost to 0 for an Infinity bow.
 */
@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {

    @ModifyExpressionValue(
        method = "useAmmo",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processAmmoUse("
                   + "Lnet/minecraft/server/level/ServerLevel;"
                   + "Lnet/minecraft/world/item/ItemStack;"
                   + "Lnet/minecraft/world/item/ItemStack;I)I"))
    private static int thatmakessense$infinityAllArrows(
            int ammoToUse, ItemStack weapon, ItemStack projectile, LivingEntity holder, boolean forceInfinite) {
        if (ammoToUse > 0
                && ModConfig.get().infinityAllArrows.enabled
                && projectile.is(ItemTags.ARROWS)
                && thatmakessense$hasInfinity(weapon)) {
            return 0;
        }
        return ammoToUse;
    }

    @Unique
    private static boolean thatmakessense$hasInfinity(ItemStack weapon) {
        for (Holder<Enchantment> enchantment : weapon.getEnchantments().keySet()) {
            if (enchantment.is(Enchantments.INFINITY)) {
                return true;
            }
        }
        return false;
    }
}
