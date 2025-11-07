package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface HammerRecipeSchema {
    RecipeKey<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.key("input", ComponentRole.INPUT);
    RecipeKey<List<ItemStack>> RESULTS = ItemStackComponent.ITEM_STACK.instance().asList().key("results", ComponentRole.OUTPUT);

    RecipeSchema SCHEMA = new RecipeSchema(RESULTS, INGREDIENT);
}
