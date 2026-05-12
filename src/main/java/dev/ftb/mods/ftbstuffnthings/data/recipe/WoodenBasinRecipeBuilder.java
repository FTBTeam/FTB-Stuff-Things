package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.WoodenBasinRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class WoodenBasinRecipeBuilder extends BaseRecipeBuilder<WoodenBasinRecipe> {
    private final String inputStateStr;
    private final FluidStackTemplate fluidResult;
    private float productionChance = 1f;
    private float blockConsumeChance = 1f;
    private boolean dropItems = false;

    public WoodenBasinRecipeBuilder(String inputStateStr, FluidStackTemplate fluidResult) {
        this.inputStateStr = inputStateStr;
        this.fluidResult = fluidResult;
    }

    public WoodenBasinRecipeBuilder withProductionChance(float chance) {
        productionChance = chance;
        return this;
    }

    public WoodenBasinRecipeBuilder withBlockConsumeChance(float chance) {
        blockConsumeChance = chance;
        return this;
    }

    public WoodenBasinRecipeBuilder dropItems() {
        dropItems = true;
        return this;
    }

    @Override
    protected WoodenBasinRecipe buildRecipe() {
        return new WoodenBasinRecipe(inputStateStr, fluidResult, productionChance, blockConsumeChance, dropItems);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, fluidResult.typeHolder().unwrapKey().orElseThrow().identifier());
    }
}
