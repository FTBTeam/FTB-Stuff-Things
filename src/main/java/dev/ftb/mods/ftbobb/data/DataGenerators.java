package dev.ftb.mods.ftbobb.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new I18nGenerator(packOutput));
        generator.addProvider(event.includeClient(), new BlockStatesGenerators(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModelsGenerator(packOutput, existingFileHelper));

        generator.addProvider(event.includeServer(), new RecipesGenerator(packOutput, event.getLookupProvider()));
    }
}
