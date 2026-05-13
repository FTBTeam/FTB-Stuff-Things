package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.HammerRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class HammerRecipeBuilder extends BaseRecipeBuilder<HammerRecipe> {
    private final Ingredient ingredient;
    private final List<ItemStackTemplate> results;

    public HammerRecipeBuilder(Ingredient ingredient, List<ItemStackTemplate> results) {
        this.ingredient = ingredient;
        this.results = results;
    }

    @Override
    protected HammerRecipe buildRecipe() {
        return new HammerRecipe(ingredient, results);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.results.getFirst());
    }
}
