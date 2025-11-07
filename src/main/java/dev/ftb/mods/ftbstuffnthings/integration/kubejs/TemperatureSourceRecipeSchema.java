package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public interface TemperatureSourceRecipeSchema {
    RecipeKey<String> BLOCKSTATE_STR = StringComponent.STRING.inputKey("blockstate");
    RecipeKey<Temperature> TEMPERATURE = EnumComponent.of(FTBStuffNThings.id("temperature"), Temperature.class, StringRepresentable.fromEnum(Temperature::values)).outputKey("temperature").optional(Temperature.NORMAL);
    RecipeKey<Double> EFFICIENCY = NumberComponent.DOUBLE.otherKey("efficiency").optional(1.0);
    RecipeKey<ItemStack> DISPLAY_STACK = ItemStackComponent.ITEM_STACK.otherKey("display_item").optional(ItemStack.EMPTY);
    RecipeKey<Boolean> HIDE_FROM_JEI = BooleanComponent.BOOLEAN.otherKey("hide_from_jei").optional(false);

    RecipeSchema SCHEMA = new RecipeSchema(BLOCKSTATE_STR, TEMPERATURE, EFFICIENCY, DISPLAY_STACK, HIDE_FROM_JEI);
}
