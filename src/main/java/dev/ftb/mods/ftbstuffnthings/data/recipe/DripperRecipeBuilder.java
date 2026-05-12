package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.DripperRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

public class DripperRecipeBuilder extends BaseRecipeBuilder<DripperRecipe> {
    private final String inputStateStr;
    private final String outputStateStr;
    private final FluidStack fluid;
    private double chance = 1.0;
    private boolean consumeFluidOnFail = false;

    public DripperRecipeBuilder(String inputStateStr, String outputStateStr, FluidStack fluid) {
        this.inputStateStr = inputStateStr;
        this.outputStateStr = outputStateStr;
        this.fluid = fluid;
    }

    public DripperRecipeBuilder withChance(double chance) {
        Validate.isTrue(chance > 0.0 && chance <= 1.0, "chance must be in range (0.0 -> 1.0]");
        this.chance = chance;
        return this;
    }

    public DripperRecipeBuilder consumeFluidOnFail() {
        consumeFluidOnFail = true;
        return this;
    }

    @Override
    protected DripperRecipe buildRecipe() {
        return new DripperRecipe(inputStateStr, outputStateStr, fluid, chance, consumeFluidOnFail);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(FTBStuffNThings.MOD_ID, outputStateStr.replace(':', '_')));
    }
}
