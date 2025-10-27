package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.neoforged.neoforge.fluids.FluidStack;

public interface WoodenBasinSchema {
    RecipeKey<FluidStack> OUTPUT_FLUID = FluidStackComponent.FLUID_STACK.outputKey("fluid");
    RecipeKey<String> BLOCK_STATE_STRING = StringComponent.STRING.inputKey("input");
    RecipeKey<Float> PRODUCE_CHANCE = NumberComponent.FLOAT.inputKey("chance").optional(1f);
    RecipeKey<Float> CONSUME_CHANCE = NumberComponent.FLOAT.inputKey("block_consume_chance").optional(1f);
    RecipeKey<Boolean> DROP_ITEMS = BooleanComponent.BOOLEAN.inputKey("drop_items").optional(false);

    RecipeSchema SCHEMA = new RecipeSchema(OUTPUT_FLUID, BLOCK_STATE_STRING, PRODUCE_CHANCE, CONSUME_CHANCE, DROP_ITEMS);
}
