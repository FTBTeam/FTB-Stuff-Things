package dev.ftb.mods.ftbstuffnthings.data.recipe;


import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SluiceRecipe;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class SluiceRecipeBuilder extends BaseRecipeBuilder<SluiceRecipe> {
    private final Ingredient ingredient;
    private final List<ItemWithChance> results;
    private int maxResults;
    @Nullable
    private SizedFluidIngredient fluid;
    private float processingTimeMultiplier;
    private final HashSet<MeshType> meshTypes;

    public SluiceRecipeBuilder(Ingredient ingredient, List<ItemWithChance> results, List<MeshType> meshTypes) {
        this.ingredient = ingredient;
        this.results = results;
        this.maxResults = 4;
        this.fluid = SizedFluidIngredient.of(Fluids.WATER, 10);
        this.processingTimeMultiplier = 1F;
        this.meshTypes = new HashSet<>(meshTypes);
    }

    public SluiceRecipeBuilder maxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public SluiceRecipeBuilder fluid(FluidStack fluid) {
        this.fluid = SizedFluidIngredient.of(fluid.getFluid(), fluid.amount());
        return this;
    }

    public SluiceRecipeBuilder noFluid() {
        this.fluid = null;
        return this;
    }

    public SluiceRecipeBuilder processingTimeMultiplier(float processingTimeMultiplier) {
        this.processingTimeMultiplier = processingTimeMultiplier;
        return this;
    }

    @Override
    protected SluiceRecipe buildRecipe() {
        return new SluiceRecipe(ingredient, results, maxResults, Optional.ofNullable(fluid), processingTimeMultiplier, List.copyOf(meshTypes));
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(results.getFirst().item());
    }
}
