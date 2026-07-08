package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Heal tamed parrots with seeds. Parrots are the one common pet with no heal path - wolves and cats
 * both feed-to-heal, but a parrot's food does nothing once tamed. When an owner feeds seeds
 * ({@code #minecraft:parrot_food}) to an injured tamed parrot we restore a heart and consume one seed;
 * a full-health parrot falls through to vanilla, so feeding still toggles sitting as before. Healing
 * happens on the server; the seed is consumed unless the player is in creative.
 */
@Mixin(Parrot.class)
public abstract class ParrotMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$healWithSeeds(
            Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!ModConfig.get().healParrots.enabled) {
            return;
        }
        Parrot self = (Parrot) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        if (self.isTame() && stack.is(ItemTags.PARROT_FOOD) && self.getHealth() < self.getMaxHealth()) {
            if (self.level() instanceof ServerLevel level) {
                self.heal(2.0F);
                level.playSound(null, self.getX(), self.getY(), self.getZ(),
                        SoundEvents.PARROT_EAT, self.getSoundSource(), 1.0F, 1.0F);
            }
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
