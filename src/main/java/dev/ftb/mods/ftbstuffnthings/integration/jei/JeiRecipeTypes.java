package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.*;
import dev.ftb.mods.ftbstuffnthings.util.lootsummary.WrappedLootSummary;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class JeiRecipeTypes {
    public static final IRecipeType<JarRecipe> TEMPERED_JAR = register("jar", JarRecipe.class);
    public static final IRecipeType<TemperatureSourceRecipe> TEMPERATURE_SOURCE = register("temperature_source", TemperatureSourceRecipe.class);
    public static final IRecipeType<DripperRecipe> DRIPPER = register("dripper", DripperRecipe.class);
    public static final IRecipeType<CrookRecipe> CROOK = register("crook", CrookRecipe.class);
    public static final IRecipeType<HammerRecipe> HAMMER = register("hammer", HammerRecipe.class);
    public static final IRecipeType<FusingMachineRecipe> FUSING_MACHINE = register("fusing_machine", FusingMachineRecipe.class);
    public static final IRecipeType<SuperCoolerRecipe> SUPER_COOLER = register("super_cooler_jei", SuperCoolerRecipe.class);
    public static final IRecipeType<SluiceRecipe> SLUICE = register("sluice", SluiceRecipe.class);
    public static final IRecipeType<WoodenBasinRecipe> WOODEN_BASIN = register("wooden_basin", WoodenBasinRecipe.class);

    // special pseudo-recipe with its own recipe manager plugin
    public static final IRecipeType<WrappedLootSummary> LOOT_SUMMARY = IRecipeType.create(FTBStuffNThings.MOD_ID, "loot_summary", WrappedLootSummary.class);

    private static <T extends Recipe<?>> IRecipeType<T> register(String name, Class<T> recipeClass) {
        return IRecipeType.create(FTBStuffNThings.MOD_ID, name, recipeClass);
    }
}
