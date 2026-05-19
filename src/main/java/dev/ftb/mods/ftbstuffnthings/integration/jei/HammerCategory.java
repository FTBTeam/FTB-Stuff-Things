package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.HammerRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

public class HammerCategory extends BaseStuffCategory<HammerRecipe> {
    public HammerCategory() {
        super(JeiRecipeTypes.HAMMER,
                Component.translatable(ItemsRegistry.STONE_HAMMER.get().getDescriptionId()),
                guiHelper().drawableBuilder(bgTexture("jei_hammer.png"),
                        0, 0, 156, 62).setTextureSize(180, 62).build(),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemsRegistry.STONE_HAMMER.get().getDefaultInstance())
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HammerRecipe hammerRecipe, IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).add(hammerRecipe.getIngredient());

        for (int i = 0; i < hammerRecipe.getResults().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 28 + (i % 7 * 18), 5 + i / 7 * 18).add(hammerRecipe.getResults().get(i));
        }
    }
}
