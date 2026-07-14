package com.kestalkayden.thatmakessense.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Silk Touch -&gt; Budding Amethyst. Vanilla's budding amethyst loot table is empty - the block drops
 * nothing, even with Silk Touch, to keep amethyst geodes non-renewable. This lets a Silk Touch tool
 * harvest the block, which is the intuitive behaviour ("Silk Touch grabs the block intact").
 *
 * <p>Injects at the head of the tool-aware {@link Block#getDrops} - the path player mining takes via
 * {@code dropResources} - and returns the block itself when it's budding amethyst, mined with Silk
 * Touch, and the feature is enabled. The config is read at drop time, so the toggle is live (no
 * {@code /reload} needed). Shared by both loaders; the cheap {@code instanceof} short-circuit keeps
 * the per-drop overhead negligible for every other block.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
        at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$silkTouchBuddingAmethyst(
            BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool,
            CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!ModConfig.get().silkTouchBudding.enabled) {
            return;
        }
        if (!(state.getBlock() instanceof BuddingAmethystBlock)) {
            return;
        }
        Holder<Enchantment> silkTouch = level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0) {
            cir.setReturnValue(List.of(new ItemStack(state.getBlock())));
        }
    }
}
