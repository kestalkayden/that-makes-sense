package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bonemeal grows nether wart, cactus, and sugar cane - none of which are {@code BonemealableBlock} in
 * vanilla, so {@code growCrop} normally fails on them. Injecting at the head of {@code growCrop} lets
 * us grow those plants and hand a {@code true} back up to {@code useOn}, which then runs vanilla's own
 * success path (consume vibration + the green growth particles via {@code levelEvent 1505}). We consume
 * the bonemeal only when growth actually happens (nether wart below max age; a cactus/sugar-cane column
 * shorter than three with clear space above), so bonemeal is never wasted on a maxed-out plant.
 * Growth is applied server-side only; the client just predicts the result.
 */
@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {

    /** Cactus and sugar cane grow to at most three blocks tall, matching natural growth. */
    @Unique
    private static final int THATMAKESSENSE_MAX_COLUMN = 3;

    @Inject(method = "growCrop", at = @At("HEAD"), cancellable = true)
    private static void thatmakessense$bonemealExtraPlants(
            ItemStack stack, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.get().bonemealExtras.enabled) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        boolean grew;
        if (block == Blocks.NETHER_WART) {
            grew = thatmakessense$growNetherWart(level, pos, state);
        } else if (block == Blocks.CACTUS || block == Blocks.SUGAR_CANE) {
            grew = thatmakessense$growColumn(level, pos, state);
        } else {
            return; // not one of ours - let vanilla decide
        }
        if (grew && level instanceof ServerLevel) {
            stack.shrink(1);
        }
        cir.setReturnValue(grew);
    }

    @Unique
    private static boolean thatmakessense$growNetherWart(Level level, BlockPos pos, BlockState state) {
        int age = state.getValue(NetherWartBlock.AGE);
        if (age >= NetherWartBlock.MAX_AGE) {
            return false;
        }
        if (level instanceof ServerLevel) {
            level.setBlock(pos, state.setValue(NetherWartBlock.AGE, age + 1), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Unique
    private static boolean thatmakessense$growColumn(Level level, BlockPos pos, BlockState state) {
        Block plant = state.getBlock();
        BlockPos top = pos;
        while (level.getBlockState(top.above()).is(plant)) {
            top = top.above();
        }
        int height = 1;
        BlockPos below = top.below();
        while (level.getBlockState(below).is(plant)) {
            height++;
            below = below.below();
        }
        BlockPos target = top.above();
        BlockState grown = plant.defaultBlockState();
        if (height >= THATMAKESSENSE_MAX_COLUMN || !level.isEmptyBlock(target) || !grown.canSurvive(level, target)) {
            return false;
        }
        if (level instanceof ServerLevel) {
            level.setBlockAndUpdate(target, grown);
        }
        return true;
    }
}
