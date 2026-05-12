package dev.ftb.mods.ftbstuffnthings.blocks.supercooler;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineMenu;
import dev.ftb.mods.ftbstuffnthings.capabilities.IOStackHandler;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class SuperCoolerMenu extends AbstractMachineMenu<SuperCoolerBlockEntity> {
    public SuperCoolerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public SuperCoolerMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ContentRegistry.SUPER_COOLER_MENU.get(), windowId, playerInventory, pos);

        int startY = 10;
        if (getBlockEntity() != null && getBlockEntity().getItemHandler() instanceof IOStackHandler handler) {
            ItemStacksResourceHandler input = handler.getInput();
            ItemStacksResourceHandler output = handler.getOutput();
            addSlot(new ResourceHandlerSlot(input, input::set,0, 42, startY));
            addSlot(new ResourceHandlerSlot(input, input::set, 1, 42, startY + 18));
            addSlot(new ResourceHandlerSlot(input, input::set, 2, 42, startY + (18 * 2)));
            addSlot(new ExtractOnlySlot(output, output::set,0, 122, startY + 19));
        }

        addPlayerSlots(playerInventory, 8, 84);

        addDataSlots(containerData);
    }

    public static class ExtractOnlySlot extends ResourceHandlerSlot {
        public ExtractOnlySlot(ResourceHandler<ItemResource> itemHandler, IndexModifier<ItemResource> modifier, int index, int xPosition, int yPosition) {
            super(itemHandler, modifier, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
