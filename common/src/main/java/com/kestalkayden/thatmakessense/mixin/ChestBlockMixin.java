package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.feature.CopperChestMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Copper Chest Rows (double). A double copper chest combines two enlarged 4-row halves into an 8-row
 * / 72-slot menu, replacing the vanilla 6-row double menu (which caps at 54 and would fail
 * {@code ChestMenu.checkContainerSize} once each half reports 36 slots).
 *
 * <p>Injects at the head of {@link ChestBlock#getMenuProvider} - a stable, overridable method - rather
 * than the anonymous menu-provider combiner. Only takes over when both halves are enlarged (combined
 * size == 72); otherwise it defers to vanilla, so a pair opened while the feature was off stays a
 * normal 6-row double. The provider mirrors vanilla's double-open logic (lock check, loot unpacking,
 * combined display name) but hands back {@link CopperChestMenus#createServerMenu} at 8 rows.
 */
@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin {

    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$copperDoubleMenu(
            BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir) {
        // Driven off the frozen combined size (below), never live config: a chest's size is fixed at
        // construction, so a pair enlarged while the feature was on must still open at 8 rows even if
        // the feature was toggled off afterwards - otherwise vanilla's 6-row menu would reject the 72
        // slots. New behaviour applies on reload, matching ChestBlockEntityMixin.
        if (!(state.getBlock() instanceof CopperChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return;
        }

        BlockEntity thisBe = level.getBlockEntity(pos);
        BlockEntity otherBe = level.getBlockEntity(ChestBlock.getConnectedBlockPos(pos, state));
        if (!(thisBe instanceof ChestBlockEntity c1) || !(otherBe instanceof ChestBlockEntity c2)) {
            return;
        }

        // Vanilla combiner order: the RIGHT half is FIRST (top rows), LEFT is SECOND (bottom rows).
        boolean thisIsRight = state.getValue(ChestBlock.TYPE) == ChestType.RIGHT;
        final ChestBlockEntity first = thisIsRight ? c1 : c2;
        final ChestBlockEntity second = thisIsRight ? c2 : c1;
        final Container container = new CompoundContainer(first, second);

        // Both halves must be enlarged (72). If not, let vanilla build its 6-row double (54) as usual.
        if (container.getContainerSize() != CopperChestMenus.SLOTS) {
            return;
        }

        cir.setReturnValue(new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
                if (first.canOpen(player) && second.canOpen(player)) {
                    first.unpackLootTable(playerInv.player);
                    second.unpackLootTable(playerInv.player);
                    return CopperChestMenus.createServerMenu(syncId, playerInv, container);
                }
                return null;
            }

            @Override
            public Component getDisplayName() {
                if (first.hasCustomName()) {
                    return first.getDisplayName();
                }
                return second.hasCustomName() ? second.getDisplayName() : Component.translatable("container.chestDouble");
            }
        });
    }
}
