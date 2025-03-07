package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new I18nGenerator(packOutput));
        generator.addProvider(event.includeClient(), new BlockModelsGenerator(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new BlockStatesGenerators(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModelsGenerator(packOutput, existingFileHelper));

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new BlockTagsGenerator(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ItemTagsGenerator(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new RecipesGenerator(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new LootTablesGenerator(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new LootModifiersGenerator(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AdvancementsGenerator(packOutput, lookupProvider,existingFileHelper));

        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, DamageTypesGenerator::bootstrap);

        DatapackBuiltinEntriesProvider provider = generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, builder, Set.of(FTBStuffNThings.MODID)));

        generator.addProvider(event.includeServer(), new DamageTypeTagsGenerator(packOutput, provider.getRegistryProvider(), existingFileHelper));
    }
}
