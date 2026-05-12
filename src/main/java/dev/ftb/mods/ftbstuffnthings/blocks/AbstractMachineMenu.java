/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ftb.mods.ftbstuffnthings.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractMachineMenu<T extends AbstractMachineBlockEntity> extends AbstractContainerMenu {
    @Nullable
    private final T blockEntity;
    private int playerSlotsStart;
    protected ContainerData containerData;

    public AbstractMachineMenu(MenuType type, int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
        this(type, windowId, invPlayer, getTilePos(extraData));
    }

    public AbstractMachineMenu(MenuType type, int windowId, Inventory invPlayer) {
        this(type, windowId, invPlayer, (BlockPos) null);
    }

    public AbstractMachineMenu(MenuType type, int windowId, Inventory invPlayer, BlockPos blockPos) {
        super(type, windowId);
        if (blockPos != null) {
            BlockEntity te0 = invPlayer.player.level().getBlockEntity(blockPos);
            if (te0 instanceof AbstractMachineBlockEntity) {
                //noinspection unchecked
                blockEntity = (T) te0;  // safe cast: T extends AbstractMachineBlockEntity, and we're doing an instanceof
                containerData = blockEntity.getContainerData();
            } else {
                blockEntity = null;
            }
        } else {
            blockEntity = null;
        }
    }

    public static BlockPos getTilePos(FriendlyByteBuf buf) {
        return buf.readBlockPos();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (blockEntity != null) {
            blockEntity.syncFluidTank(true);
        }
    }

    protected void addPlayerSlots(Inventory inventoryPlayer, int yOffset) {
        addPlayerSlots(inventoryPlayer, 8, yOffset);
    }

    protected void addPlayerSlots(Inventory inventoryPlayer, int xOffset, int yOffset) {
        playerSlotsStart = slots.size();

        // Add the player's inventory slots to the container
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(inventoryPlayer, col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int hotbarIdx = 0; hotbarIdx < 9; ++hotbarIdx) {
            addSlot(new Slot(inventoryPlayer, hotbarIdx, xOffset + hotbarIdx * 18, yOffset + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        Slot srcSlot = slots.get(slot);
        if (srcSlot == null || !srcSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getItem().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot < playerSlotsStart) {
            if (!moveItemStackToHotbarOrInventory(srcStack, playerSlotsStart))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(srcStack, 0, playerSlotsStart, false))
                return ItemStack.EMPTY;
        }

        srcSlot.set(srcStack);
        srcSlot.onQuickCraft(srcStack, copyOfSrcStack);
        srcSlot.onTake(player, srcStack);

        return copyOfSrcStack;
    }

    boolean moveItemStackToHotbarOrInventory(ItemStack stack, int startIndex) {
        return moveItemStackTo(stack, startIndex + 27, startIndex + 36, false)
                || moveItemStackTo(stack, startIndex, startIndex + 27, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) {
            return false;
        }
        Vec3 position = player.position();
        return this.blockEntity.getBlockPos().distManhattan(BlockPos.containing(position)) <= 8;
    }

    @Nullable
    public T getBlockEntity() {
        return blockEntity;
    }
}
