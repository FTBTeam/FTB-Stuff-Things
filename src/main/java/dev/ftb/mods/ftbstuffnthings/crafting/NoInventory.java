package dev.ftb.mods.ftbstuffnthings.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public enum NoInventory implements RecipeInput {
	INSTANCE;

	@Override
	public ItemStack getItem(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public int size() {
		return 0;
	}
}
