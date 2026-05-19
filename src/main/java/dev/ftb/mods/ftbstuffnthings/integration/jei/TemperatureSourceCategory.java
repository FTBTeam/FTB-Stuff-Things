package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.TemperatureSourceRecipe;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class TemperatureSourceCategory extends BaseStuffCategory<TemperatureSourceRecipe> {
    public TemperatureSourceCategory() {
        super(JeiRecipeTypes.TEMPERATURE_SOURCE,
                Component.translatable("ftbstuff.temperature_source"),
                guiHelper().drawableBuilder(bgTexture("jei_temperature_source.png"), 0, 0, 71, 30)
                        .setTextureSize(128, 64).build(),
                guiHelper().createDrawableIngredient(FTBStuffIngredientTypes.TEMPERATURE, Temperature.HOT)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TemperatureSourceRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 7)
                .add(FTBStuffIngredientTypes.TEMPERATURE, recipe.getTemperature())
                .addRichTooltipCallback((recipeSlotView, tooltip) ->
                        tooltip.add(Component.translatable("ftbstuff.efficiency", recipe.getTemperatureAndEfficiency().formatEfficiency())));

        ItemStack itemStack = recipe.getDisplayStack()
                .map(ItemStackTemplate::create)
                .orElse(ItemStack.EMPTY);

        if (!itemStack.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 3, 7)
                    .add(VanillaTypes.ITEM_STACK, itemStack);
        }
    }
}
