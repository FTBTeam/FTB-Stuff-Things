package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemModelsGenerator extends ItemModelProvider {
    private static final ResourceLocation GENERATED = ResourceLocation.parse("item/generated");

    public ItemModelsGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FTBStuffNThings.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        String path = BlocksRegistry.PUMP.getKey().location().getPath();
        this.getBuilder(path).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + path + "_on")));

        withExistingParent("tube", FTBStuffNThings.id("block/tube_inv"));
        withExistingParent("jar", FTBStuffNThings.id("block/jar"));
        withExistingParent("tempered_jar", FTBStuffNThings.id("block/tempered_jar_normal"));
        withExistingParent("auto_processing_block", FTBStuffNThings.id("block/auto_processing_block"));
        withExistingParent("blue_magma_block", FTBStuffNThings.id("block/blue_magma_block"));
        withExistingParent("creative_low_temperature_source", modLoc("block/creative_low_temperature_source"));
        withExistingParent("creative_high_temperature_source", modLoc("block/creative_high_temperature_source"));
        withExistingParent("creative_subzero_temperature_source", modLoc("block/creative_subzero_temperature_source"));
        withExistingParent("cast_iron_block", modLoc("block/cast_iron_block"));
        withExistingParent("iron_auto_hammer", modLoc("block/iron_auto_hammer"));
        withExistingParent("gold_auto_hammer", modLoc("block/gold_auto_hammer"));
        withExistingParent("diamond_auto_hammer", modLoc("block/diamond_auto_hammer"));
        withExistingParent("netherite_auto_hammer", modLoc("block/netherite_auto_hammer"));

        withExistingParent("stone_cobblestone_generator", modLoc("block/stone_cobblestone_generator"));
        withExistingParent("iron_cobblestone_generator", modLoc("block/iron_cobblestone_generator"));
        withExistingParent("gold_cobblestone_generator", modLoc("block/gold_cobblestone_generator"));
        withExistingParent("diamond_cobblestone_generator", modLoc("block/diamond_cobblestone_generator"));
        withExistingParent("netherite_cobblestone_generator", modLoc("block/netherite_cobblestone_generator"));

        simpleItem(ItemsRegistry.FLUID_CAPSULE, "item/fluid_container_base", "item/fluid_container_overlay");
        simpleItem(ItemsRegistry.DRIPPER, "item/dripper");
        simpleItem(ItemsRegistry.WATER_BOWL, "item/water_bowl");

        simpleItem(ItemsRegistry.CAST_IRON_GEAR, "item/cast_iron_gear");
        simpleItem(ItemsRegistry.CAST_IRON_INGOT, "item/cast_iron_ingot");
        simpleItem(ItemsRegistry.CAST_IRON_NUGGET, "item/cast_iron_nugget");
        simpleItem(ItemsRegistry.TEMPERED_GLASS, "item/tempered_glass");

        simpleItem(ItemsRegistry.STONE_HAMMER, "item/stone_hammer");
        simpleItem(ItemsRegistry.IRON_HAMMER, "item/iron_hammer");
        simpleItem(ItemsRegistry.GOLD_HAMMER, "item/gold_hammer");
        simpleItem(ItemsRegistry.DIAMOND_HAMMER, "item/diamond_hammer");
        simpleItem(ItemsRegistry.NETHERITE_HAMMER, "item/netherite_hammer");

        simpleItem(ItemsRegistry.CROOK, "item/stone_crook");
        simpleItem(ItemsRegistry.STONE_ROD, "item/stone_rod");
    }

    private ItemModelBuilder simpleItem(DeferredItem<? extends Item> item, String... textures) {
        return simpleItem(item.getId(), textures);
    }

    private ItemModelBuilder simpleItem(ResourceLocation itemKey, String... textures) {
        ItemModelBuilder builder = withExistingParent(itemKey.getPath(), GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }
}
