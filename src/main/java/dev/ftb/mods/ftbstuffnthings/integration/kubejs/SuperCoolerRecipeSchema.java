package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TinyMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public interface SuperCoolerRecipeSchema {
    RecipeKey<List<Ingredient>> INGREDIENTS = IngredientComponent.INGREDIENT.asList().inputKey("inputs");
    RecipeKey<SizedFluidIngredient> FLUID = SizedFluidIngredientComponent.FLAT.inputKey("fluid");
    RecipeKey<TinyMap<String, Integer>> ENERGY = new MapRecipeComponent<>(StringComponent.ANY, NumberComponent.INT, false)
            .otherKey("energy");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, FLUID, ENERGY);
}
