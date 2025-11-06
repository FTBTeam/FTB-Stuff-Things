package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TinyMap;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public interface FusingMachineRecipeSchema {
    RecipeKey<List<Ingredient>> INGREDIENTS = IngredientComponent.INGREDIENT.asList().inputKey("inputs");
    RecipeKey<FluidStack> RESULT = FluidStackComponent.FLUID_STACK.outputKey("result");
    RecipeKey<TinyMap<String, Integer>> ENERGY = new MapRecipeComponent<>(StringComponent.ANY, NumberComponent.INT, false)
            .otherKey("energy");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, ENERGY);
}
