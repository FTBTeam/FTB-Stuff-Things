package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

public class KubeJSIntegration implements KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(FTBStuffNThings.id("jar"), JarRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("temperature_source"), TemperatureSourceRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("hammer"), HammerRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("crook"), CrookRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("fusing_machine"), FusingMachineRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("supercooler"), SuperCoolerRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("sluice"), SluiceRecipeSchema.SCHEMA);
        registry.register(FTBStuffNThings.id("wooden_basin"), WoodenBasinSchema.SCHEMA);

        FTBStuffNThings.LOGGER.info("Registered KubeJS recipe schemas");
    }
}
