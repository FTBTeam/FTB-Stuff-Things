package dev.ftb.mods.ftbstuffnthings.registry;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.crafting.FTBStuffRecipeType;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class RecipesRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS
            = DeferredRegister.create(Registries.RECIPE_SERIALIZER, FTBStuffNThings.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES
            = DeferredRegister.create(Registries.RECIPE_TYPE, FTBStuffNThings.MOD_ID);

    // ---------------------------------------------

    public static final Supplier<RecipeType<JarRecipe>> TEMPERED_JAR_TYPE
            = registerType("jar", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<JarRecipe>> TEMPERED_JAR_SERIALIZER
            = RECIPE_SERIALIZERS.register("jar", () -> JarRecipe.SERIALIZER);

    public static final Supplier<RecipeType<TemperatureSourceRecipe>> TEMPERATURE_SOURCE_TYPE
            = registerType("temperature_source", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<TemperatureSourceRecipe>> TEMPERATURE_SOURCE_SERIALIZER
            = RECIPE_SERIALIZERS.register("temperature_source", () -> TemperatureSourceRecipe.SERIALIZER);

    public static final Supplier<RecipeType<DripperRecipe>> DRIP_TYPE
            = registerType("dripper", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<DripperRecipe>> DRIP_SERIALIZER
            = RECIPE_SERIALIZERS.register("dripper", () -> DripperRecipe.SERIALIZER);

    public static final Supplier<RecipeType<WoodenBasinRecipe>> WOODEN_BASIN_TYPE
            = registerType("wooden_basin", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<WoodenBasinRecipe>> WOODEN_BASIN_SERIALIZER
            = RECIPE_SERIALIZERS.register("wooden_basin", () -> WoodenBasinRecipe.SERIALIZER);

    public static final Supplier<RecipeType<CrookRecipe>> CROOK_TYPE
            = registerType("crook", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<CrookRecipe>> CROOK_SERIALIZER
            = RECIPE_SERIALIZERS.register("crook", () -> CrookRecipe.SERIALIZER);

    public static final Supplier<RecipeType<SluiceRecipe>> SLUICE_TYPE
            = registerType("sluice", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<SluiceRecipe>> SLUICE_SERIALIZER
            = RECIPE_SERIALIZERS.register("sluice", () -> SluiceRecipe.SERIALIZER);

    public static final Supplier<RecipeType<HammerRecipe>> HAMMER_TYPE
            = registerType("hammer", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<HammerRecipe>> HAMMER_SERIALIZER
            = RECIPE_SERIALIZERS.register("hammer", () -> HammerRecipe.SERIALIZER);

    public static final Supplier<RecipeType<FusingMachineRecipe>> FUSING_MACHINE_TYPE
            = registerType("fusing_machine", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<FusingMachineRecipe>> FUSING_MACHINE_SERIALIZER
            = RECIPE_SERIALIZERS.register("fusing_machine", () -> FusingMachineRecipe.SERIALIZER);

    public static final Supplier<RecipeType<SuperCoolerRecipe>> SUPER_COOLER_TYPE
            = registerType("supercooler", FTBStuffRecipeType::new);
    public static final Supplier<RecipeSerializer<SuperCoolerRecipe>> SUPER_COOLER_SERIALIZER
            = RECIPE_SERIALIZERS.register("supercooler", () -> SuperCoolerRecipe.SERIALIZER);

    // ---------------------------------------------

    public static void init(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }

    private static <T extends RecipeType<?>> Supplier<T> registerType(String name, Function<String, T> factory) {
        return RECIPE_TYPES.register(name, () -> factory.apply(name));
    }
}
