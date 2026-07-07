package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Copper Chest Rows. Every copper chest holds one extra row - 4 rows / 36 slots instead of the
 * vanilla 3 / 27. Copper chests reuse the vanilla {@link ChestBlockEntity}, so we key off the block
 * class: {@link CopperChestBlock} is the base for both the waxed variants and the weathering subclass,
 * covering all eight blocks with one {@code instanceof}.
 *
 * <p>Always on - the feature is not toggleable, so a copper chest's size is a stable function of its
 * block (never config) and can never disagree with the backing {@link #items} list. That keeps
 * hoppers/comparators safe and means chests are never resized after the fact, so items are never
 * silently dropped.
 *
 * <p>A single chest opens the native 4-row {@code GENERIC_9x4} screen (handled here). A double copper
 * chest combines two enlarged halves into 72 slots; that 8-row menu is built by {@code ChestBlockMixin}.
 */
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    /** 4 rows for a single copper chest. */
    @Unique
    private static final int THATMAKESSENSE$SINGLE_SLOTS = 36;

    /** True for copper chests; set once at construction (block type never changes for a chest). */
    @Unique
    private boolean thatmakessense$enlarged = false;

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At("TAIL"))
    private void thatmakessense$enlargeCopper(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof CopperChestBlock) {
            thatmakessense$enlarged = true;
            this.items = NonNullList.withSize(THATMAKESSENSE$SINGLE_SLOTS, ItemStack.EMPTY);
        }
    }

    @Inject(method = "getContainerSize", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$size(CallbackInfoReturnable<Integer> cir) {
        if (thatmakessense$enlarged) {
            cir.setReturnValue(THATMAKESSENSE$SINGLE_SLOTS);
        }
    }

    @Inject(
        method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
        at = @At("HEAD"), cancellable = true)
    private void thatmakessense$singleMenu(int syncId, Inventory inv, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (thatmakessense$enlarged) {
            cir.setReturnValue(new ChestMenu(
                MenuType.GENERIC_9x4, syncId, inv, (ChestBlockEntity) (Object) this, 4));
        }
    }
}
