package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public interface JarRecipeSchema {
    RecipeKey<List<ItemStack>> OUTPUT_ITEMS = ItemStackComponent.ITEM_STACK.instance().asList().outputKey("output_items");
    RecipeKey<List<FluidStack>> OUTPUT_FLUIDS = FluidStackComponent.FLUID_STACK.instance().asList().outputKey("output_fluids");
    RecipeKey<List<SizedIngredient>> INPUT_ITEMS = SizedIngredientComponent.FLAT.instance().asList().inputKey("input_items").optional(List.of());
    RecipeKey<List<SizedFluidIngredient>> INPUT_FLUIDS = SizedFluidIngredientComponent.FLAT.instance().asList().inputKey("input_fluids").optional(List.of());
    RecipeKey<Temperature> TEMPERATURE = EnumComponent.of(FTBStuffNThings.id("temperature"), Temperature.class, StringRepresentable.fromEnum(Temperature::values)).otherKey("temperature").optional(Temperature.NORMAL);
    RecipeKey<Integer> TIME = NumberComponent.INT.key("time", ComponentRole.OTHER).optional(200);
    RecipeKey<Boolean> CAN_REPEAT = BooleanComponent.BOOLEAN.key("can_repeat", ComponentRole.OTHER).optional(true);
    RecipeKey<String> STAGE = StringComponent.STRING.key("stage", ComponentRole.OTHER).optional("");

    RecipeSchema SCHEMA = new RecipeSchema(OUTPUT_ITEMS, OUTPUT_FLUIDS, INPUT_ITEMS, INPUT_FLUIDS, TEMPERATURE, TIME, CAN_REPEAT, STAGE);
}
