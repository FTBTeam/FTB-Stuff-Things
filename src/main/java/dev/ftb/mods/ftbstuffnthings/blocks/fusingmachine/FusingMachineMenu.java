package dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineMenu;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.Objects;

public class FusingMachineMenu extends AbstractMachineMenu<FusingMachineBlockEntity> {
    public FusingMachineMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public FusingMachineMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ContentRegistry.FUSING_MACHINE_MENU.get(), windowId, playerInventory, pos);

        FusingMachineBlockEntity fusingMachine = Objects.requireNonNull(getBlockEntity());
        var itemHandler = fusingMachine.getItemHandler();
        addSlot(new ResourceHandlerSlot(itemHandler, fusingMachine::indexModifier, 0, 43, 27));
        addSlot(new ResourceHandlerSlot(itemHandler, fusingMachine::indexModifier, 1, 43 + 18, 27));

        addPlayerSlots(playerInventory, 8, 84);

        addDataSlots(containerData);
    }
}
