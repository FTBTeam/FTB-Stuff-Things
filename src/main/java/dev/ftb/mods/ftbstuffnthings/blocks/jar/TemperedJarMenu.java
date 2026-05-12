package dev.ftb.mods.ftbstuffnthings.blocks.jar;

import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class TemperedJarMenu extends AbstractContainerMenu {
    public static final int PLAYER_INV_Y = 132;
    public static final int INPUT_ITEMS_Y = 36;

    private final TemperedJarBlockEntity jar;

    public static TemperedJarMenu fromNetwork(int containerId, Inventory invPlayer, FriendlyByteBuf extraData) {
        TemperedJarMenu menu = new TemperedJarMenu(containerId, invPlayer, extraData.readBlockPos());
        Identifier recipeId = extraData.readOptional(FriendlyByteBuf::readIdentifier).orElse(null);
        menu.getJar().setCurrentRecipeId(recipeId);
        return menu;
    }

    public TemperedJarMenu(int containerId, Inventory invPlayer, BlockPos blockPos) {
        super(ContentRegistry.TEMPERED_JAR_MENU.get(), containerId);

        jar = invPlayer.player.level().getBlockEntity(blockPos, BlockEntitiesRegistry.TEMPERED_JAR.get())
                .orElseThrow(() -> new IllegalStateException("tempered jar missing at " + blockPos));

        // player's main inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                int slot = 9 + y * 9 + x;
                addSlot(new Slot(invPlayer, slot, 8 + x * 18, PLAYER_INV_Y + y * 18));
            }
        }
        // player's hotbar
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(invPlayer, x, 8 + x * 18, PLAYER_INV_Y + 58));
        }

        // item input slots
        for (int y = 0; y < 3; y++) {
            addSlot(new ResourceHandlerSlot(jar.getInputItemHandler(), jar::itemIndexModifer, y, 17, INPUT_ITEMS_Y + 18 * y));
        }

        addDataSlots(jar.getContainerData());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            resultStack = stackInSlot.copy();
            if (index < 36) {
                if (!moveItemStackTo(stackInSlot, 36, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 27, 36, false)
                    && !moveItemStackTo(stackInSlot, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return resultStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return !jar.isRemoved() && player.distanceToSqr(Vec3.atCenterOf(jar.getBlockPos())) < 64;
    }

    public TemperedJarBlockEntity getJar() {
        return jar;
    }
}
