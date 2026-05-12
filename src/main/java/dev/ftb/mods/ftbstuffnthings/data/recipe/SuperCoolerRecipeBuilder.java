package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SuperCoolerRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class SuperCoolerRecipeBuilder extends BaseRecipeBuilder<SuperCoolerRecipe> {
    private final List<Ingredient> itemInputs;
    private final SizedFluidIngredient fluidInput;
    private final int fePerTick;
    private final int ticks;
    private final ItemStack result;

    public SuperCoolerRecipeBuilder(List<Ingredient> itemInputs, SizedFluidIngredient fluidInput, int fePerTick, int ticks, ItemStack result) {
        this.itemInputs = itemInputs;
        this.fluidInput = fluidInput;
        this.fePerTick = fePerTick;
        this.ticks = ticks;
        this.result = result;
    }

    @Override
    protected SuperCoolerRecipe buildRecipe() {
        return new SuperCoolerRecipe(itemInputs, fluidInput, new EnergyRequirement(fePerTick, ticks), ItemStackTemplate.fromNonEmptyStack(result));
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(result);
    }
}
