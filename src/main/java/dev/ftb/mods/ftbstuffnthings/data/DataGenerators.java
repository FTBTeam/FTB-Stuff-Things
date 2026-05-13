package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FTBStuffNThings.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(I18nGenerator::new);
        event.createProvider(ModelGenerator::new);

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.createProvider(RecipesGenerator.Runner::new);
        event.createBlockAndItemTags(BlockTagsGenerator::new, ItemTagsGenerator::new);
        event.createProvider(LootTablesGenerator::new);
        event.createProvider(LootModifiersGenerator::new);
        event.createProvider(AdvancementsGenerator::new);

        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, DamageTypesGenerator::bootstrap);

        DataGenerator generator = event.getGenerator();
        DatapackBuiltinEntriesProvider provider = generator.addProvider(true,
                new DatapackBuiltinEntriesProvider(generator.getPackOutput(), lookupProvider, builder, Set.of(FTBStuffNThings.MOD_ID)));
        generator.addProvider(true, new DamageTypeTagsGenerator(generator.getPackOutput(), provider.getRegistryProvider()));
    }
}
