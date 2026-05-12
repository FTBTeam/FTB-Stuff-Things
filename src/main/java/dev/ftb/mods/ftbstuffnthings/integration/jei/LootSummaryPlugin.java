package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.util.lootsummary.LootSummaryCollection;
import dev.ftb.mods.ftbstuffnthings.util.lootsummary.WrappedLootSummary;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public enum LootSummaryPlugin implements ISimpleRecipeManagerPlugin<WrappedLootSummary> {
    INSTANCE;

    private static final List<WrappedLootSummary> NONE = List.of();

    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return input.getItemStack()
                .map(stack -> LootSummaryCollection.getClientSummary().getLootSummaryForInput(Block.byItem(stack.getItem())).isPresent())
                .orElse(false);
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return output.getItemStack()
                .map(stack -> !LootSummaryCollection.getClientSummary().getLootSummariesForOutput(stack).isEmpty())
                .orElse(false);
    }

    @Override
    public List<WrappedLootSummary> getRecipesForInput(ITypedIngredient<?> input) {
        return input.getItemStack().map(LootSummaryPlugin::getWrappedLootSummariesForStack).orElse(NONE);
    }

    @Override
    public List<WrappedLootSummary> getRecipesForOutput(ITypedIngredient<?> output) {
        return output.getItemStack()
                .map(stack -> LootSummaryCollection.getClientSummary().getLootSummariesForOutput(stack))
                .orElse(List.of());
    }

    @Override
    public List<WrappedLootSummary> getAllRecipes() {
        return LootSummaryCollection.getClientSummary().getLootSummariesForOutput(ItemStack.EMPTY);
    }

    private static List<WrappedLootSummary> getWrappedLootSummariesForStack(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) {
            return NONE;
        }

        return LootSummaryCollection.getClientSummary().getLootSummaryForInput(block)
                .map(summary -> List.of(new WrappedLootSummary(block, summary)))
                .orElse(NONE);
    }
}
