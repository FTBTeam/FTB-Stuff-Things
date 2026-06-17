package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public interface FusingMachineRecipeSchema {
    RecipeKey<List<Ingredient>> INGREDIENTS = IngredientComponent.INGREDIENT.instance().asList().inputKey("inputs");
    RecipeKey<FluidStack> RESULT = FluidStackComponent.FLUID_STACK.outputKey("result");
    RecipeKey<EnergyRequirement> ENERGY = EnergyRequirementComponent.TYPE.instance().otherKey("energy");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, ENERGY);
}
