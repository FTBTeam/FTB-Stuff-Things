package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SluiceRecipe;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SluiceCategory extends BaseStuffCategory<SluiceRecipe> {
    private final static Comparator<ItemWithChance> COMPARATOR = (a, b) -> (int) ((b.chance() * 100) - (a.chance() * 100));

    public SluiceCategory() {
        super(
                JeiRecipeTypes.SLUICE,
                Component.translatable("ftbstuff.sluice"),
                guiHelper().drawableBuilder(bgTexture("jei_sluice.png"),
                        0, 0, 156, 78).setTextureSize(180, 78).build(),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemsRegistry.OAK_SLUICE.get().getDefaultInstance())
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SluiceRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
                .add(recipe.getIngredient());

        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 5, 24)
                .addItemStacks(recipe.getMeshTypes().stream().map(MeshType::createItemStack).toList());

        List<FluidStack> fluidStacks = recipe.getFluid().map(MiscUtil::getFluidsForSizedIngredient).orElse(List.of());
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 42)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fluidStacks)
                .addRichTooltipCallback((_, tooltip) -> recipe.getFluid().ifPresent(f ->
                        tooltip.add(Component.translatable("ftbstuff.fluid_usage",
                                Component.literal(f.amount() + "").withStyle(ChatFormatting.YELLOW)
                        ).withStyle(ChatFormatting.GRAY))));

        List<ItemWithChance> itemWithWeights = recipe.getResults().stream().sorted(COMPARATOR).toList();
        for (int i = 0; i < itemWithWeights.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT,  28 + (i % 7 * 18), 5 + i / 7 * 24)
                    .add(itemWithWeights.get(i).item());
        }
    }

    @Override
    public void draw(SluiceRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        List<ItemWithChance> itemWithWeights = new ArrayList<>(recipe.getResults());
        itemWithWeights.sort(COMPARATOR);

        int row = 0;
        for (int i = 0; i < itemWithWeights.size(); i++) {
            if (i > 0 && i % 7 == 0) {
                row++;
            }
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(36 + (i % 7 * 18), 23.5f + (row * 24));
            poseStack.scale(.5F, .5F);
            guiGraphics.centeredText(Minecraft.getInstance().font, Math.round(itemWithWeights.get(i).chance() * 100) + "%", 0, 0, 0xFFFFFF);
            poseStack.popMatrix();
        }
    }
}
