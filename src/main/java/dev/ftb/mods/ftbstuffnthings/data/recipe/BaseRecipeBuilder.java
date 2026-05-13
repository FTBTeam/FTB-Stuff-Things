package dev.ftb.mods.ftbstuffnthings.data.recipe;

import dev.ftb.mods.ftbstuffnthings.crafting.DevEnvironmentCondition;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public abstract class BaseRecipeBuilder<T extends Recipe<?>> implements RecipeBuilder {
    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    abstract protected T buildRecipe();

    public void save(RecipeOutput recipeOutput, Identifier id) {
        save(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
    }

    public void saveTest(RecipeOutput recipeOutput, Identifier id) {
        saveTest(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        T recipe = buildRecipe();
        Identifier id1 = Identifier.fromNamespaceAndPath(id.identifier().getNamespace(), recipe.getType() + "/" + id.identifier().getPath());
        recipeOutput.accept(ResourceKey.create(Registries.RECIPE, id1), recipe, null);
    }

    public void saveTest(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        T recipe = buildRecipe();
        Identifier id1 = Identifier.fromNamespaceAndPath(id.identifier().getNamespace(), recipe.getType() + "/dev_test_" + id.identifier().getPath());
        recipeOutput.withConditions(DevEnvironmentCondition.INSTANCE).accept(ResourceKey.create(Registries.RECIPE, id1), recipe, null);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        throw new UnsupportedOperationException("Default ID generation is not supported for custom recipe types");
    }
}
