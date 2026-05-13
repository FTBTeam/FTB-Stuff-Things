package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.JarRecipe;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class TemperedJarRecipeBuilder extends BaseRecipeBuilder<JarRecipe> {
    private final List<SizedIngredient> itemsIn;
    private final List<SizedFluidIngredient> fluidsIn;
    private final List<ItemStack> itemsOut;
    private final List<FluidStack> fluidsOut;
    private final Temperature requiredTemp;
    private int time = 200;
    private String stage = "";
    private boolean canRepeat = true;

    public TemperedJarRecipeBuilder(List<SizedIngredient> itemsIn, List<SizedFluidIngredient> fluidsIn, List<ItemStack> itemsOut, List<FluidStack> fluidsOut, Temperature requiredTemp) {
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
        List<ItemStackTemplate> outItems = itemsOut.stream().map(ItemStackTemplate::fromNonEmptyStack).toList();
        List<FluidStackTemplate> outFluids = fluidsOut.stream().map(fs -> new FluidStackTemplate(fs.getFluid(), fs.getAmount())).toList();
        return new JarRecipe(itemsIn, fluidsIn, outItems, outFluids, requiredTemp, time, canRepeat, stage);
    }
}
