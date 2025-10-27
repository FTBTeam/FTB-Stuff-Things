package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public interface SluiceRecipeSchema {
    RecipeKey<List<ItemWithChance>> RESULTS = ItemWithChanceComponent.TYPE.instance().asList().key("results", ComponentRole.OUTPUT);
    RecipeKey<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.key("input", ComponentRole.INPUT);
    RecipeKey<SizedFluidIngredient> FLUID = SizedFluidIngredientComponent.FLAT.inputKey("fluid");
    RecipeKey<List<MeshType>> MESH_TYPES = EnumComponent.of(FTBStuffNThings.id("mesh_type"), MeshType.class, MeshType.CODEC).instance().asList().otherKey("mesh_types");
    RecipeKey<Integer> MAX_RESULTS = NumberComponent.INT.key("max_results", ComponentRole.OTHER).optional(4);
    RecipeKey<Float> TIME = NumberComponent.FLOAT.key("processing_time_multiplier", ComponentRole.OTHER).optional(1F);

    RecipeSchema SCHEMA = new RecipeSchema(RESULTS, INGREDIENT, FLUID, MESH_TYPES, MAX_RESULTS, TIME);
}
