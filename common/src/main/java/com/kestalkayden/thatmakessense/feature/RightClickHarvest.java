package com.kestalkayden.thatmakessense.feature;

import java.util.List;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Right-click harvest + replant. Right-clicking a fully-grown crop drops its produce and resets it to
 * age 0 in place, so it regrows without a break-and-replant cycle. Covers every {@link CropBlock}
 * (wheat, carrots, potatoes, beetroot, and modded crops that extend it) plus nether wart.
 *
 * <p>To match a manual harvest, one "seed" (the crop's pick-block item) is withheld from the drops as
 * the replant cost - so the net yield equals break + replant, not break + free seed.
 *
 * <p>Loader-agnostic; each loader's block-interaction hook calls {@link #tryHarvest}. That returns
 * true when the block is a harvestable mature crop, so the caller consumes the interaction (clean arm
 * swing, no block placement); the drops + replant themselves only run server-side. Non-crops and
 * immature crops return false and fall through to vanilla (bone meal etc. still works).
 */
public final class RightClickHarvest {

    private RightClickHarvest() {}

    public static boolean tryHarvest(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level == null || pos == null || player == null) return false;
        if (!ModConfig.get().rightClickHarvest.enabled) return false;
        // Off-hand and sneaking are left to vanilla (sneak-right-click places/uses items).
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) return false;

        BlockState state = level.getBlockState(pos);
        BlockState replanted = replantState(state);
        if (replanted == null) return false; // not a mature crop we handle

        if (level instanceof ServerLevel serverLevel) {
            harvest(serverLevel, pos, state, replanted, player);
        }
        // Consume on both sides so the click doesn't also try to place a held block.
        return true;
    }

    /** The age-0 state to replant to, or null if {@code state} isn't a mature crop we handle. */
    private static BlockState replantState(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state) ? crop.getStateForAge(0) : null;
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE
                ? state.setValue(NetherWartBlock.AGE, 0)
                : null;
        }
        return null;
    }

    private static void harvest(ServerLevel level, BlockPos pos, BlockState state, BlockState replanted, Player player) {
        // Drops use the held item as the tool, so a Fortune hoe still boosts the yield.
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());

        // Withhold one replant item (the crop's pick-block form) so the net matches break + replant.
        ItemStack seed = state.getBlock().getCloneItemStack(level, pos, state);
        if (!seed.isEmpty()) {
            for (ItemStack drop : drops) {
                if (!drop.isEmpty() && ItemStack.isSameItem(drop, seed)) {
                    drop.shrink(1);
                    break;
                }
            }
        }

        level.setBlockAndUpdate(pos, replanted);
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }
    }
}
