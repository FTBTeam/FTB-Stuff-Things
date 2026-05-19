package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.CrookRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

public class CrookCategory extends BaseStuffCategory<CrookRecipe> {
    private static final Comparator<ItemWithChance> COMPARATOR = (a, b) -> (int) ((b.chance() * 100) - (a.chance() * 100));

    public CrookCategory() {
        super(JeiRecipeTypes.CROOK,
                Component.translatable("item.ftbstuff.stone_crook"),
                guiHelper().drawableBuilder(bgTexture("jei_crook.png"), 0, 0, 156, 78).setTextureSize(180, 78).build(),
                guiHelper().createDrawableItemStack(ItemsRegistry.CROOK.toStack())
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrookRecipe crookRecipe, IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
                .add(crookRecipe.getIngredient())
                .addRichTooltipCallback((_, tooltip) -> {
                    if (crookRecipe.getResults().size() > 1 && crookRecipe.getMax() > 0) {
                        tooltip.add(Component.translatable("ftbstuff.crook.limit", crookRecipe.getMax()));
                    }
                });

        List<ItemWithChance> outputs = crookRecipe.getResults().stream().sorted(COMPARATOR).toList();
        for (int i = 0; i < outputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 28 + (i % 7 * 18), 5 + i / 7 * 24)
                    .add(outputs.get(i).item());
        }
    }

    @Override
    public void draw(CrookRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

        List<ItemWithChance> outputs = recipe.getResults().stream().sorted(COMPARATOR).toList();

        int row = 0;
        for (int i = 0; i < outputs.size(); i++) {
            if (i > 0 && i % 7 == 0) {
                row++;
            }
            var stack = graphics.pose();
            stack.pushMatrix();
            stack.translate(36 + (i % 7 * 18), 23.5f + (row * 24));
            stack.scale(.5F, .5F);
            graphics.centeredText(Minecraft.getInstance().font, Math.round(outputs.get(i).chance() * 100) + "%", 0, 0, 0xFFFFFFFF);
            stack.popMatrix();
        }
    }

}
