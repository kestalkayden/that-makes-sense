package com.kestalkayden.thatmakessense.feature;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Loader-agnostic "double doors" behaviour: when a player toggles a door, mirror the open/close onto
 * the door immediately to its left and right (relative to facing), flipping the partner's hinge when
 * needed so the pair swings apart like a real double door.
 *
 * <p>Pure vanilla API only - no Fabric/NeoForge imports - so this class lives in {@code common} and is
 * compiled into both loader jars as well as the standalone {@code :common} purity check. Each loader
 * forwards its block-interaction hook (Fabric {@code UseBlockCallback}, NeoForge
 * {@code PlayerInteractEvent.RightClickBlock}) to {@link #onDoorUse}.
 *
 * <h3>Timing: predict, don't wait</h3>
 * <p>The interaction hooks fire on the server <em>just before</em> vanilla toggles the clicked door. A
 * hand right-click always flips {@code OPEN}, so rather than waiting to observe the committed state
 * (which lands a tick or two later and makes the partner visibly trail), we predict the new state
 * ({@code !open}) and apply it to the neighbours in the same tick. The clicked door and its partner
 * therefore move together.
 *
 * <h3>Fault tolerance</h3>
 * <ul>
 *   <li>Server-authoritative; client and off-hand calls are ignored.</li>
 *   <li>Only hand-openable doors are mirrored - iron doors (redstone-only) are skipped both as the
 *       clicked door and as a partner, so prediction can't desync against a door that won't toggle.</li>
 *   <li>Sneaking is skipped (the player is placing/using an item, not opening the door).</li>
 *   <li>Partners are changed programmatically, which does not re-fire the interaction hook - no
 *       recursion; the effect is exactly one hop (the clicked door's direct left/right neighbours).</li>
 *   <li>The interaction is never consumed - worst case, behaviour degrades to plain vanilla doors.</li>
 * </ul>
 */
public final class DoubleDoors {

    private DoubleDoors() {}

    /**
     * Record a door right-click from a loader interaction hook and mirror it onto the neighbours.
     * Never consumes the interaction - vanilla still toggles the clicked door itself.
     */
    public static void onDoorUse(Level level, BlockPos clicked, Player player, InteractionHand hand) {
        if (level == null || level.isClientSide()) return;
        if (!ModConfig.get().doubleDoors.enabled) return;
        if (clicked == null || player == null) return;
        if (hand != InteractionHand.MAIN_HAND) return;
        // Sneaking suppresses block use (the player is placing/using an item), so the door won't toggle.
        if (player.isShiftKeyDown()) return;

        BlockState clickedState = level.getBlockState(clicked);
        if (!(clickedState.getBlock() instanceof DoorBlock)) return;
        // Iron (redstone-only) doors don't open by hand, so a hand click won't toggle them and
        // mirroring would desync - ignore them as the clicked door.
        if (clickedState.is(Blocks.IRON_DOOR)) return;

        // Normalise to the door's lower half so neighbour math and state reads are consistent.
        BlockPos lowerPos = clickedState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER
            ? clicked.below()
            : clicked.immutable();
        BlockState lower = level.getBlockState(lowerPos);
        if (!(lower.getBlock() instanceof DoorBlock)) return;

        // The hook runs before vanilla toggles the clicked door; a hand click flips OPEN. Predict the
        // resulting state and apply it to the neighbours now, so the whole door moves in one tick.
        boolean newOpen = !lower.getValue(BlockStateProperties.OPEN);
        Direction facing = lower.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide hinge = lower.getValue(BlockStateProperties.DOOR_HINGE);

        syncNeighbor(level, lowerPos.relative(facing.getClockWise()), newOpen, facing, hinge, player);
        syncNeighbor(level, lowerPos.relative(facing.getCounterClockWise()), newOpen, facing, hinge, player);
    }

    /** Mirror the clicked door onto the neighbouring door whose lower half is at {@code neighborLower},
     *  if there is a hand-openable one. Matches its open state, and - when it shares the clicked door's
     *  facing but has the same hinge - flips its hinge to the opposite side so the pair swings apart
     *  like a real double door. */
    private static void syncNeighbor(Level level, BlockPos neighborLower, boolean open,
                                     Direction primaryFacing, DoorHingeSide primaryHinge, Player player) {
        BlockState state = level.getBlockState(neighborLower);
        if (!(state.getBlock() instanceof DoorBlock door)) return;
        if (state.is(Blocks.IRON_DOOR)) return;  // leave redstone-only doors to redstone
        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER) return;

        // A proper double door has the two leaves on opposite hinges. If this neighbour faces the same
        // way as the clicked door but shares its hinge, flip it so they mirror.
        boolean flipHinge = state.getValue(BlockStateProperties.HORIZONTAL_FACING) == primaryFacing
                && state.getValue(BlockStateProperties.DOOR_HINGE) == primaryHinge;
        boolean changeOpen = state.getValue(BlockStateProperties.OPEN) != open;
        if (!flipHinge && !changeOpen) return;  // already a matching, mirrored pair - nothing to do

        BlockPos upperPos = neighborLower.above();

        // Step 1 - mirror the hinge on both halves (silently), preserving the current open state.
        if (flipHinge) {
            DoorHingeSide opposite = state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT
                ? DoorHingeSide.RIGHT
                : DoorHingeSide.LEFT;
            level.setBlock(neighborLower, state.setValue(BlockStateProperties.DOOR_HINGE, opposite), 10);
            BlockState upper = level.getBlockState(upperPos);
            if (upper.getBlock() == door
                    && upper.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                level.setBlock(upperPos, upper.setValue(BlockStateProperties.DOOR_HINGE, opposite), 10);
            }
            state = level.getBlockState(neighborLower);  // refresh with the new hinge
        }

        // Step 2 - open/close via the canonical path (plays the door's sound, emits the game event).
        if (state.getValue(BlockStateProperties.OPEN) != open) {
            door.setOpen(player, level, state, neighborLower, open);
            // Belt-and-suspenders: ensure the upper half's open state matches too.
            BlockState upper = level.getBlockState(upperPos);
            if (upper.getBlock() == door
                    && upper.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER
                    && upper.getValue(BlockStateProperties.OPEN) != open) {
                level.setBlock(upperPos, upper.setValue(BlockStateProperties.OPEN, open), 10);
            }
        }
    }
}
