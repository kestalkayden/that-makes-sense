package com.kestalkayden.thatmakessense.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.level.block.NetherPortalBlock;

/**
 * Faster nether portal transition. Vanilla makes a player stand in the portal for the
 * {@code playersNetherPortalDefaultDelay} game rule's worth of ticks (~4s) before travelling;
 * {@code NetherPortalBlock.getPortalTransitionTime} reports that wait. We clamp the reported value down
 * to a configurable number of ticks (default 0 = instant), never raising it. Mobs already transition
 * instantly, so this just brings players in line. Server-side.
 */
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    @ModifyReturnValue(method = "getPortalTransitionTime", at = @At("RETURN"))
    private int thatmakessense$fasterPortal(int original) {
        ModConfig.FasterNetherPortal cfg = ModConfig.get().fasterNetherPortal;
        return cfg.enabled ? Math.min(original, Math.max(0, cfg.delayTicks)) : original;
    }
}
