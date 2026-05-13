package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.TemperatureSourceRecipe;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class TemperatureSourceRecipeBuilder extends BaseRecipeBuilder<TemperatureSourceRecipe> {
    private final String blockstateStr;
    private final Temperature temperature;
    private final double efficiency;
    @Nullable
    private ItemStackTemplate displayStack = null;
    private boolean hideFromJEI = false;

    public TemperatureSourceRecipeBuilder(String blockstateStr, Temperature temperature, double efficiency) {
        this.blockstateStr = blockstateStr;
        this.temperature = temperature;
        this.efficiency = efficiency;
    }

    public TemperatureSourceRecipeBuilder(BlockState blockstate, Temperature temperature, double efficiency) {
        this(BlockStateParser.serialize(blockstate), temperature, efficiency);
    }

    public TemperatureSourceRecipeBuilder(Block block, Temperature temperature, double efficiency) {
        this(BlockStateParser.serialize(block.defaultBlockState()), temperature, efficiency);
    }

    public TemperatureSourceRecipeBuilder withDisplayItem(ItemStackTemplate stack) {
        this.displayStack = stack;
        return this;
    }

    public TemperatureSourceRecipeBuilder hideFromJEI() {
        this.hideFromJEI = true;
        return this;
    }

    @Override
    protected TemperatureSourceRecipe buildRecipe() {
        return new TemperatureSourceRecipe(blockstateStr, temperature, efficiency, Optional.ofNullable(displayStack), hideFromJEI);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(displayStack != null ? displayStack : new ItemStackTemplate(Items.AIR));
    }
}
