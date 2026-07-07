package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * No Crop Trample (Fabric). Wraps the static {@code turnToDirt(...)} invocation inside
 * {@link FarmlandBlock#fallOn} so we can skip it without disturbing the surrounding fall-damage path
 * (the {@code super.fallOn} call still runs). MixinExtras' {@code @WrapOperation} is bundled with
 * Fabric Loader, so this needs no extra runtime dependency.
 *
 * <p>NeoForge gets the same behaviour from a cancellable {@code FarmlandTrampleEvent} listener; both
 * paths route through {@link ModConfig#shouldPreventTrampling(Entity)} in :common.
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @WrapOperation(
        method = "fallOn",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FarmlandBlock;turnToDirt(" +
                     "Lnet/minecraft/world/entity/Entity;" +
                     "Lnet/minecraft/world/level/block/state/BlockState;" +
                     "Lnet/minecraft/world/level/Level;" +
                     "Lnet/minecraft/core/BlockPos;)V"))
    private void thatmakessense$skipTurnToDirt(
            Entity entity, BlockState state, Level level, BlockPos pos,
            Operation<Void> original) {
        if (!ModConfig.get().shouldPreventTrampling(entity)) {
            original.call(entity, state, level, pos);
        }
    }
}
