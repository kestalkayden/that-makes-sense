package com.kestalkayden.thatmakessense.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import org.spongepowered.asm.mixin.Mixin;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Totem of Undying from Inventory. Vanilla only consumes a death-protection item held in a hand;
 * this lets a player be saved by a totem anywhere in the main inventory.
 *
 * <p>Rather than reimplement the save (effects, animation, sound, advancement criteria - which could
 * drift from vanilla), it temporarily stages an inventory totem into the off-hand, lets vanilla's own
 * {@code checkTotemDeathProtection} consume it and apply the exact effect, then restores the previous
 * off-hand item and returns any leftover to the inventory. Runs server-side (the method is only
 * invoked on the authoritative side). Skips when a hand already holds a totem, so it never double-dips.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapMethod(method = "checkTotemDeathProtection")
    private boolean thatmakessense$totemFromInventory(DamageSource source, Operation<Boolean> original) {
        if (!ModConfig.get().totemFromInventory.enabled || !((Object) this instanceof Player player)) {
            return original.call(source);
        }
        // A hand already carries a totem: let vanilla handle it (no double-dipping).
        if (player.getMainHandItem().has(DataComponents.DEATH_PROTECTION)
                || player.getOffhandItem().has(DataComponents.DEATH_PROTECTION)) {
            return original.call(source);
        }

        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        int slot = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).has(DataComponents.DEATH_PROTECTION)) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            return original.call(source);
        }

        // Stage the inventory totem into the (totem-free) off-hand so vanilla consumes it and applies
        // its exact death-save; clear the slot first so it never exists in two places at once.
        ItemStack held = player.getOffhandItem();
        ItemStack totem = items.get(slot);
        player.setItemInHand(InteractionHand.OFF_HAND, totem);
        items.set(slot, ItemStack.EMPTY);

        boolean saved = original.call(source);

        // Restore the previous off-hand item and return whatever remains of the totem to its slot.
        ItemStack leftover = player.getOffhandItem();
        player.setItemInHand(InteractionHand.OFF_HAND, held);
        items.set(slot, leftover);
        if (player.containerMenu != null) {
            player.containerMenu.broadcastChanges();
        }
        return saved;
    }
}
