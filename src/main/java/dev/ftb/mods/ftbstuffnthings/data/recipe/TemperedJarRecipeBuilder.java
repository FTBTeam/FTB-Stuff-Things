package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.JarRecipe;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class TemperedJarRecipeBuilder extends BaseRecipeBuilder<JarRecipe> {
    private final List<SizedIngredient> itemsIn;
    private final List<SizedFluidIngredient> fluidsIn;
    private final List<ItemStackTemplate> itemsOut;
    private final List<FluidStackTemplate> fluidsOut;
    private final Temperature requiredTemp;
    private int time = 200;
    private String stage = "";
    private boolean canRepeat = true;

    public TemperedJarRecipeBuilder(List<SizedIngredient> itemsIn, List<SizedFluidIngredient> fluidsIn, List<ItemStackTemplate> itemsOut, List<FluidStackTemplate> fluidsOut, Temperature requiredTemp) {
        this.itemsIn = itemsIn;
        this.fluidsIn = fluidsIn;
        this.itemsOut = itemsOut;
        this.fluidsOut = fluidsOut;
        this.requiredTemp = requiredTemp;
    }

    public TemperedJarRecipeBuilder withTime(int time) {
        this.time = time;
        return this;
    }

    public TemperedJarRecipeBuilder notRepeatable() {
        canRepeat = false;
        return this;
    }

    public TemperedJarRecipeBuilder withStage(String stage) {
        this.stage = stage;
        return this;
    }

    @Override
    protected JarRecipe buildRecipe() {
        return new JarRecipe(itemsIn, fluidsIn, itemsOut, fluidsOut, requiredTemp, time, canRepeat, stage);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(itemsOut.getFirst());
    }
}
