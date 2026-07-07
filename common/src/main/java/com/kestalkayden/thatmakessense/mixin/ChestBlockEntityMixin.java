package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Copper Chest Rows (single). A single copper chest holds one extra row - 4 rows / 36 slots instead
 * of the vanilla 3 / 27. Copper chests reuse the vanilla {@link ChestBlockEntity}, so we discriminate
 * by block class: {@link CopperChestBlock} is the base for both the waxed variants and the weathering
 * subclass, so a single {@code instanceof} covers all eight blocks with no tag lookup.
 *
 * <p>Scoped to {@link ChestType#SINGLE} on purpose. A double copper chest combines two block entities
 * into one {@code CompoundContainer} and opens the vanilla 6-row {@code GENERIC_9x6} menu, which
 * asserts the container is exactly 54 slots; leaving each half at 27 keeps that path valid until the
 * separate 8-row double-chest feature (custom menu type + screen) lands.
 *
 * <p>Shared by both loaders - it targets vanilla official names and needs no client code, since an
 * enlarged single opens the native {@code GENERIC_9x4} screen automatically.
 */
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Unique
    private static final int THATMAKESSENSE$ENLARGED_ROWS = 4;

    @Unique
    private boolean thatmakessense$isEnlargedSingleCopper() {
        if (!ModConfig.get().copperChest.enabled) {
            return false;
        }
        BlockState state = ((BlockEntity) (Object) this).getBlockState();
        return state.getBlock() instanceof CopperChestBlock
            && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE;
    }

    /**
     * Grow a freshly-placed single copper chest's backing list to 4 rows. Chests loaded from disk are
     * resized by {@code ChestBlockEntity.loadAdditional}, which reads the enlarged
     * {@link #thatmakessense$enlargeSize} below - so this only covers the never-yet-saved case, but
     * without it slot access past index 26 on a just-placed chest would throw.
     */
    @Inject(
        method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At("TAIL"))
    private void thatmakessense$growItems(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (thatmakessense$isEnlargedSingleCopper()) {
            this.items = NonNullList.withSize(THATMAKESSENSE$ENLARGED_ROWS * 9, ItemStack.EMPTY);
        }
    }

    @Inject(method = "getContainerSize", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$enlargeSize(CallbackInfoReturnable<Integer> cir) {
        if (thatmakessense$isEnlargedSingleCopper()) {
            cir.setReturnValue(THATMAKESSENSE$ENLARGED_ROWS * 9);
        }
    }

    @Inject(
        method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
        at = @At("HEAD"), cancellable = true)
    private void thatmakessense$enlargeMenu(int syncId, Inventory inv, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (thatmakessense$isEnlargedSingleCopper()) {
            cir.setReturnValue(new ChestMenu(
                MenuType.GENERIC_9x4, syncId, inv, (ChestBlockEntity) (Object) this, THATMAKESSENSE$ENLARGED_ROWS));
        }
    }
}
