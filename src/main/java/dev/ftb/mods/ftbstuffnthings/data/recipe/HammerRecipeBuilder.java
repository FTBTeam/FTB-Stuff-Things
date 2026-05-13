package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.HammerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class HammerRecipeBuilder extends BaseRecipeBuilder<HammerRecipe> {
    private final Ingredient ingredient;
    private final List<ItemStack> results;

    public HammerRecipeBuilder(Ingredient ingredient, List<ItemStack> results) {
        this.ingredient = ingredient;
        this.results = results;
    }

    @Override
    protected HammerRecipe buildRecipe() {
        return new HammerRecipe(ingredient, results.stream().map(ItemStackTemplate::fromNonEmptyStack).toList());
    }
}
