package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface CrookRecipeSchema {
    RecipeKey<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.key("input", ComponentRole.INPUT);
    RecipeKey<List<ItemWithChance>> RESULTS = ItemWithChanceComponent.TYPE.instance().asList().key("results", ComponentRole.OUTPUT);
    RecipeKey<Integer> MAX = NumberComponent.INT.key("max", ComponentRole.OTHER).optional(0);
    RecipeKey<Boolean> REPLACE_DROPS = BooleanComponent.BOOLEAN.key("replace_drops", ComponentRole.OTHER).optional(true);

    RecipeSchema SCHEMA = new RecipeSchema(RESULTS, INGREDIENT, MAX, REPLACE_DROPS);
}
