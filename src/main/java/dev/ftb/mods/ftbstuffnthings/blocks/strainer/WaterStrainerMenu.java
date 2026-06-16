package dev.ftb.mods.ftbstuffnthings.blocks.strainer;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineMenu;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class WaterStrainerMenu extends AbstractMachineMenu<WaterStrainerBlockEntity> {
    public WaterStrainerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTile(playerInventory.player, buffer));
    }

    public WaterStrainerMenu(int containerId, Inventory playerInv, BlockEntity be) {
        super(ContentRegistry.WATER_STRAINER_MENU.get(), containerId, playerInv, be);

        IItemHandler itemHandler = Objects.requireNonNull(blockEntity.getItemHandler());

        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new OutputOnlySlot(itemHandler, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        addPlayerSlots(playerInv, 8, 85);
    }

    private static class OutputOnlySlot extends SlotItemHandler {
        OutputOnlySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return false;
        }
    }
}
