package dev.ftb.mods.ftbstuffnthings.blocks.supercooler;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineMenu;
import dev.ftb.mods.ftbstuffnthings.capabilities.IOStackHandler;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SuperCoolerMenu extends AbstractMachineMenu<SuperCoolerBlockEntity> {
    public SuperCoolerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public SuperCoolerMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ContentRegistry.SUPER_COOLER_MENU.get(), windowId, playerInventory, pos);

        IOStackHandler handler = Objects.requireNonNull(blockEntity.getItemHandler());
        int startY = 10;
        addSlot(new SlotItemHandler(handler.getInput(), 0, 42, startY));
        addSlot(new SlotItemHandler(handler.getInput(), 1, 42, startY + 18));
        addSlot(new SlotItemHandler(handler.getInput(), 2, 42, startY + (18 * 2)));
        addSlot(new ExtractOnlySlot(handler.getOutput(), 0, 122, startY + 19));

        addPlayerSlots(playerInventory, 8, 84);

        addDataSlots(containerData);
    }

    public static class ExtractOnlySlot extends SlotItemHandler {
        public ExtractOnlySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}
