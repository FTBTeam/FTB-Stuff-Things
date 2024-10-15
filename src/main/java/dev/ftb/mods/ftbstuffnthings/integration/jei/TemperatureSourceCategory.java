package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.TemperatureSourceRecipe;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

public class TemperatureSourceCategory extends BaseOBBCategory<TemperatureSourceRecipe> {
    protected TemperatureSourceCategory() {
        super(RecipeTypes.TEMPERATURE_SOURCE,
                Component.translatable("ftbstuff.temperature_source"),
                guiHelper().drawableBuilder(bgTexture("jei_temperature_source.png"), 0, 0, 71, 30)
                        .setTextureSize(128, 64).build(),
                guiHelper().createDrawableIngredient(OBBIngredientTypes.TEMPERATURE, Temperature.HOT)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TemperatureSourceRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 7)
                .addIngredient(OBBIngredientTypes.TEMPERATURE, recipe.getTemperature())
                .addRichTooltipCallback((recipeSlotView, tooltip) ->
                        tooltip.add(Component.translatable("ftbstuff.efficiency", recipe.getTemperatureAndEfficiency().formatEfficiency())));

        if (!recipe.getDisplayStack().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 3, 7)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.getDisplayStack());
        }
    }
}
