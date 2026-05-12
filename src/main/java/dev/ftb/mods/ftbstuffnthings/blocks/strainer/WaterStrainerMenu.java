package dev.ftb.mods.ftbstuffnthings.blocks.strainer;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineMenu;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.Objects;

public class WaterStrainerMenu extends AbstractMachineMenu<WaterStrainerBlockEntity> {
    public WaterStrainerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, AbstractMachineMenu.getTilePos(buffer));
    }

    public WaterStrainerMenu(int containerId, Inventory playerInv, BlockPos pos) {
        super(ContentRegistry.WATER_STRAINER_MENU.get(), containerId, playerInv, pos);

        WaterStrainerBlockEntity strainer = getBlockEntity();
        if (strainer != null) {
            ResourceHandler<ItemResource> itemHandler = Objects.requireNonNull(strainer.getItemHandler());
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 9; k++) {
                    this.addSlot(new OutputOnlySlot(itemHandler, strainer::indexModifier, k + j * 9, 8 + k * 18, 18 + j * 18));
                }
            }
        }

        addPlayerSlots(playerInv, 8, 85);
    }

    private static class OutputOnlySlot extends ResourceHandlerSlot {
        OutputOnlySlot(ResourceHandler<ItemResource> itemHandler, IndexModifier<ItemResource> indexModifier, int index, int xPosition, int yPosition) {
            super(itemHandler, indexModifier, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
