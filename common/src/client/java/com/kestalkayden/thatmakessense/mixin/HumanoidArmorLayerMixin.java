package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Hide Armor (client-side, your own model only).
 *
 * <p>{@link HumanoidArmorLayer#submit} is the layer that draws all four armor pieces for any humanoid.
 * When the feature is on and toggled hidden, we cancel it for the local player only - identified by
 * matching the render state's entity id ({@link AvatarRenderState#id}, set from {@code entity.getId()})
 * against the client player's id. The armor stays fully equipped and protective; this only changes what
 * you see (third-person view and the inventory paperdoll). Other players and mobs render normally, and
 * other clients still see your armor.
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$hideOwnArmor(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            HumanoidRenderState state,
            float yRot,
            float xRot,
            CallbackInfo ci) {
        ModConfig.HideArmor cfg = ModConfig.get().hideArmor;
        if (!cfg.enabled || !cfg.hidden) {
            return;
        }
        if (state instanceof AvatarRenderState avatar) {
            LocalPlayer self = Minecraft.getInstance().player;
            if (self != null && avatar.id == self.getId()) {
                ci.cancel();
            }
        }
    }
}
