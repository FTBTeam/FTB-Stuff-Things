package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.client.screens.TemperedJarScreen;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.JarRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.TemperatureAndEfficiency;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TemperedJarCategory extends BaseStuffCategory<JarRecipe> {
    protected TemperedJarCategory() {
        super(RecipeTypes.TEMPERED_JAR,
                Component.translatable(BlocksRegistry.TEMPERED_JAR.get().getDescriptionId()),
                guiHelper().drawableBuilder(bgTexture("jei_tempered_jar.png"), 0, 0, 150, 18)
                        .setTextureSize(256, 32).build(),
                guiHelper().createDrawableItemStack(new ItemStack(ItemsRegistry.TEMPERED_JAR.get()))
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JarRecipe recipe, IFocusGroup focuses) {
        // we assume here that there are no more than 3 inputs & 3 outputs
        // - see JarRecipe.Serializer#validateRecipe

        int nFluidsIn = recipe.getInputFluids().size();
        int nItemsIn = recipe.getInputItems().size();
        int nFluidsOut = recipe.getOutputFluids().size();
        int nItemsOut = recipe.getOutputItems().size();

        for (int i = 0; i < nFluidsIn; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + i * 20, 1)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.getInputFluids().get(i).getFluids()))
                    .setOverlay(new FluidAmountDrawable(recipe.getInputFluids().get(i).amount()), 0, 0);
        }
        for (int i = 0; i < nItemsIn; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + (i + nFluidsIn) * 20, 1)
                    .addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getInputItems().get(i).getItems()));
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 67, 1)
                .addIngredient(FTBStuffIngredientTypes.TEMPERATURE, recipe.getTemperature())
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    String time = String.format("%.1f", recipe.getTime() / 20f);
                    tooltip.add(Component.translatable(FTBStuffNThings.MODID + ".processing_time", time));
                });

        for (int i = 0; i < nFluidsOut; i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 93 + i * 20, 1)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getOutputFluids().get(i))
                    .setOverlay(new FluidAmountDrawable(recipe.getOutputFluids().get(i).getAmount()), 0, 0);
        }
        for (int i = 0; i < nItemsOut; i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 93 + (i + nFluidsOut) * 20, 1)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutputItems().get(i));
        }
    }

    public static List<JarRecipe> sortRecipes(List<JarRecipe> jarRecipes) {
        return jarRecipes.stream().sorted().toList();
    }

    enum ContainerHandler implements IGuiContainerHandler<TemperedJarScreen> {
        INSTANCE;

        @Override
        public Collection<IGuiClickableArea> getGuiClickableAreas(TemperedJarScreen containerScreen, double guiMouseX, double guiMouseY) {
            return List.of(
                    IGuiClickableArea.createBasic(TemperedJarScreen.JEI_AREA.getX(), TemperedJarScreen.JEI_AREA.getY(),
                            TemperedJarScreen.JEI_AREA.getWidth(), TemperedJarScreen.JEI_AREA.getHeight(), RecipeTypes.TEMPERED_JAR),
                    new TemperatureClickableGuiArea(containerScreen)
            );
        }
    }

    private record TemperatureClickableGuiArea(TemperedJarScreen jarScreen) implements IGuiClickableArea {
        @Override
        public Rect2i getArea() {
            return TemperedJarScreen.TEMPERATURE_AREA;
        }

        @Override
        public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
            recipesGui.showTypes(List.of(RecipeTypes.TEMPERATURE_SOURCE));
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            TemperatureAndEfficiency tempEff = jarScreen.getMenu().getJar().getTemperature();
            tooltip.add(Component.translatable("ftbstuff.temperature", tempEff.temperature().getName()));
            tooltip.add(Component.translatable("ftbstuff.efficiency", tempEff.formatEfficiency()));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("ftbstuff.temperature_source.click"));
        }
    }
}
