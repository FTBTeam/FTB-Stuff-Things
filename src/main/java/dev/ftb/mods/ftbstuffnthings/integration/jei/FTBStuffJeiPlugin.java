package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.client.FTBStuffNThingsClient;
import dev.ftb.mods.ftbstuffnthings.client.screens.FusingMachineScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.SuperCoolerScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.TemperedJarScreen;
import dev.ftb.mods.ftbstuffnthings.crafting.IHideableRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.TemperatureSourceRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@JeiPlugin
public class FTBStuffJeiPlugin implements IModPlugin {
    @Nullable
    static IJeiHelpers jeiHelpers;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        jeiRuntime.getIngredientManager().addIngredientsAtRuntime(FTBStuffIngredientTypes.TEMPERATURE, Arrays.asList(Temperature.values()));
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(FTBStuffIngredientTypes.TEMPERATURE,
                Arrays.asList(Temperature.values()),
                TemperatureHelper.INSTANCE,
                TemperatureRenderer.INSTANCE,
                StringRepresentable.fromEnum(Temperature::values)
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();

        registration.addRecipeCategories(
                new TemperedJarCategory(),
                new TemperatureSourceCategory(),
                new DripperCategory(),
                new HammerCategory(),
                new FusingMachineCategory(),
                new SuperCoolerCategory(),
                new CrookCategory(),
                new SluiceCategory(),
                new LootSummaryCategory(),
                new WoodenBasinCategory()
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        addRecipeType(registration, RecipesRegistry.TEMPERED_JAR_TYPE.get(), JeiRecipeTypes.TEMPERED_JAR, TemperedJarCategory::sortRecipes);
        addRecipeType(registration, RecipesRegistry.TEMPERATURE_SOURCE_TYPE.get(), JeiRecipeTypes.TEMPERATURE_SOURCE, TemperatureSourceRecipe::sortRecipes);
        addRecipeType(registration, RecipesRegistry.DRIP_TYPE.get(), JeiRecipeTypes.DRIPPER);
        addRecipeType(registration, RecipesRegistry.CROOK_TYPE.get(), JeiRecipeTypes.CROOK);
        addRecipeType(registration, RecipesRegistry.HAMMER_TYPE.get(), JeiRecipeTypes.HAMMER);
        addRecipeType(registration, RecipesRegistry.FUSING_MACHINE_TYPE.get(), JeiRecipeTypes.FUSING_MACHINE);
        addRecipeType(registration, RecipesRegistry.SUPER_COOLER_TYPE.get(), JeiRecipeTypes.SUPER_COOLER);
        addRecipeType(registration, RecipesRegistry.SLUICE_TYPE.get(), JeiRecipeTypes.SLUICE);
        addRecipeType(registration, RecipesRegistry.WOODEN_BASIN_TYPE.get(), JeiRecipeTypes.WOODEN_BASIN);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(JeiRecipeTypes.TEMPERED_JAR, ItemsRegistry.TEMPERED_JAR.toStack());
        registration.addCraftingStation(JeiRecipeTypes.TEMPERATURE_SOURCE, ItemsRegistry.CREATIVE_HOT_TEMPERATURE_SOURCE.toStack());
        registration.addCraftingStation(JeiRecipeTypes.TEMPERATURE_SOURCE, ItemsRegistry.CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE.toStack());
        registration.addCraftingStation(JeiRecipeTypes.TEMPERATURE_SOURCE, ItemsRegistry.CREATIVE_CHILLED_TEMPERATURE_SOURCE.toStack());
        registration.addCraftingStation(JeiRecipeTypes.CROOK, ItemsRegistry.CROOK.toStack());
        registration.addCraftingStation(JeiRecipeTypes.DRIPPER, ItemsRegistry.DRIPPER.toStack());
        registration.addCraftingStation(JeiRecipeTypes.FUSING_MACHINE, ItemsRegistry.FUSING_MACHINE.toStack());
        registration.addCraftingStation(JeiRecipeTypes.SUPER_COOLER, ItemsRegistry.SUPER_COOLER.toStack());
        registration.addCraftingStation(JeiRecipeTypes.WOODEN_BASIN, ItemsRegistry.WOODEN_BASIN.toStack());

        for (var item : ItemsRegistry.ALL_HAMMERS) {
            registration.addCraftingStation(JeiRecipeTypes.HAMMER, item.toStack());
        }
        for (var block : BlocksRegistry.ALL_AUTO_HAMMERS) {
            registration.addCraftingStation(JeiRecipeTypes.HAMMER, block.toStack());
        }
        for (var block : BlocksRegistry.ALL_SLUICES) {
            registration.addCraftingStation(JeiRecipeTypes.SLUICE, block.toStack());
        }

//        BlocksRegistry.waterStrainers().forEach(b -> registration.addRecipeCatalyst(b.toStack(), RecipeTypes.LOOT_SUMMARY));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(TemperedJarScreen.class, TemperedJarCategory.ContainerHandler.INSTANCE);
        registration.addGuiContainerHandler(FusingMachineScreen.class, FusingMachineCategory.ContainerHandler.INSTANCE);
        registration.addGuiContainerHandler(SuperCoolerScreen.class, SuperCoolerCategory.ContainerHandler.INSTANCE);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addSimpleRecipeManagerPlugin(JeiRecipeTypes.LOOT_SUMMARY, LootSummaryPlugin.INSTANCE);
    }

    @Override
    public Identifier getPluginUid() {
        return FTBStuffNThings.id("jei_plugin");
    }

    private <I extends RecipeInput, T extends Recipe<I>> void addRecipeType(IRecipeRegistration registration, RecipeType<T> mcRecipeType, IRecipeType<T> jeiRecipeType) {
        addRecipeType(registration, mcRecipeType, jeiRecipeType, Function.identity());
    }

    private <I extends RecipeInput, T extends Recipe<I>> void addRecipeType(IRecipeRegistration registration, RecipeType<T> mcRecipeType, IRecipeType<T> jeiRecipeType, Function<List<T>, List<T>> postProcessor) {
        var recipeMap = FTBStuffNThingsClient.getInstance().getRecipeMap();

        List<T> recipes = recipeMap.byType(mcRecipeType).stream()
                .map(RecipeHolder::value)
                .filter(IHideableRecipe::shouldShow)
                .toList();
        registration.addRecipes(jeiRecipeType, postProcessor.apply(recipes));
    }
}
