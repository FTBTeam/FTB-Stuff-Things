package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockModelsGenerator extends BlockModelProvider {
    public BlockModelsGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FTBStuffNThings.MODID, existingFileHelper);
    }

    private ResourceLocation getLoc(String loc) {
        return ResourceLocation.fromNamespaceAndPath(FTBStuffNThings.MODID, "block/" + loc);
    }

    private ResourceLocation getMCLoc(String loc) {
        return ResourceLocation.withDefaultNamespace("block/" + loc);
    }

    @Override
    protected void registerModels() {
        makeSluice("oak");
        makeSluice("spruce");
        makeSluice("birch");
        makeSluice("jungle");
        makeSluice("acacia");
        makeSluice("dark_oak");
        makeSluice("mangrove");
        makeSluice("cherry");
        makeSluice("pale_oak");
        makeSluice("crimson");
        makeSluice("warped");
        makeSluice("bamboo");

        makeSluice("iron");
        makeSluice("diamond");
        makeSluice("netherite");

        makeGenerator("cobblestone");
        makeGenerator("basalt");
    }

    private void makeSluice(String type) {
        singleTexture("block/" + type + "_sluice_body", getLoc("sluice_body"), "0", getLoc("sluice/" + type + "_sluice"));
        singleTexture("block/" + type + "_sluice_front", getLoc("sluice_front"), "0", getLoc("sluice/" + type + "_sluice"));
    }

    private void makeGenerator(String type) {
        withExistingParent("block/stone_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/stone")).texture("particle", getMCLoc("cobblestone"));
        withExistingParent("block/iron_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/iron")).texture("particle", getMCLoc("iron_block"));
        withExistingParent("block/gold_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/gold")).texture("particle", getMCLoc("gold_block"));
        withExistingParent("block/diamond_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/diamond")).texture("particle", getMCLoc("diamond_block"));
        withExistingParent("block/netherite_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/netherite")).texture("particle", getMCLoc("netherite_block"));

    }

}
