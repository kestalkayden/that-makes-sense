package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hide Armor (client-side, your own model only).
 *
 * <p>{@link HumanoidArmorLayer#render(PoseStack, MultiBufferSource, int, LivingEntity, float, float,
 * float, float, float, float)} is the layer that draws all four armor pieces for any humanoid. Unlike
 * later versions that render off a detached "render state" snapshot, 1.21.1 hands the layer the live
 * {@link LivingEntity} directly, so we identify the local player by comparing entity ids against the
 * client player. When the feature is on and toggled hidden, we cancel it for the local player only -
 * the armor stays fully equipped and protective, this only changes what you see (third-person view and
 * the inventory paperdoll). Other players and mobs render normally, and other clients still see your
 * armor.
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At("HEAD"), cancellable = true)
    private void thatmakessense$hideOwnArmor(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        ModConfig.HideArmor cfg = ModConfig.get().hideArmor;
        if (!cfg.enabled || !cfg.hidden) {
            return;
        }
        LocalPlayer self = Minecraft.getInstance().player;
        if (self != null && entity.getId() == self.getId()) {
            ci.cancel();
        }
    }
}
