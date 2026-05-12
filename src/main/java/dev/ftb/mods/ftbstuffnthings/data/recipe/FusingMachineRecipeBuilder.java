package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.FusingMachineRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.List;

public class FusingMachineRecipeBuilder extends BaseRecipeBuilder<FusingMachineRecipe> {
    private final List<Ingredient> inputs;
    private final FluidStackTemplate fluidResult;
    private final int fePerTick;
    private final int ticks;

    public FusingMachineRecipeBuilder(List<Ingredient> inputs, FluidStackTemplate fluidResult, int fePerTick, int ticks) {
        this.inputs = inputs;
        this.fluidResult = fluidResult;
        this.fePerTick = fePerTick;
        this.ticks = ticks;
    }

    @Override
    protected FusingMachineRecipe buildRecipe() {
        return new FusingMachineRecipe(inputs, fluidResult, new EnergyRequirement(fePerTick, ticks));
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, fluidResult.typeHolder().unwrapKey().orElseThrow().identifier());
    }
}
