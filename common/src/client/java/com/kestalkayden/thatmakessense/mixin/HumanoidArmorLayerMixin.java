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
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

/**
 * Hide Armor (client-side, your own model only).
 *
 * <p>{@link HumanoidArmorLayer#render(PoseStack, MultiBufferSource, int, HumanoidRenderState, float,
 * float)} is the layer that draws all four armor pieces for any humanoid. Unlike 1.21.1, which hands
 * the layer the live {@link net.minecraft.world.entity.LivingEntity}, 1.21.8 renders off a detached
 * "render state" snapshot (the 1.21.5-era form, predating the later {@code submit}/render-node
 * pipeline) - {@code HumanoidArmorLayer<S extends HumanoidRenderState, ...>} only carries pose/equipment
 * data, not an entity id, on the generic {@code HumanoidRenderState} itself. Only {@link
 * PlayerRenderState} (the state used when this layer renders a real player) additionally carries
 * {@link PlayerRenderState#id}, set from {@code entity.getId()} - the same field later versions expose
 * as {@code AvatarRenderState#id}. We match that id against the client player's id to identify the
 * local player; mobs (whose render state is a plain {@code HumanoidRenderState}, not a
 * {@code PlayerRenderState}) never match and always render normally. When the feature is on and toggled
 * hidden, we cancel it for the local player only - the armor stays fully equipped and protective, this
 * only changes what you see (third-person view and the inventory paperdoll). Other players and mobs
 * render normally, and other clients still see your armor.
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At("HEAD"), cancellable = true)
    private void thatmakessense$hideOwnArmor(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            HumanoidRenderState state,
            float yRot,
            float xRot,
            CallbackInfo ci) {
        ModConfig.HideArmor cfg = ModConfig.get().hideArmor;
        if (!cfg.enabled || !cfg.hidden) {
            return;
        }
        if (state instanceof PlayerRenderState playerState) {
            LocalPlayer self = Minecraft.getInstance().player;
            if (self != null && playerState.id == self.getId()) {
                ci.cancel();
            }
        }
    }
}
