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

        makeOldSluice("iron");
        makeOldSluice("diamond");
        makeOldSluice("netherite");
    }

    private void makeSluice(String type) {
        // Loc/Name of file, parent, texturekey
        singleTexture("block/" + type + "_sluice_body", getLoc("sluice_body"), "0", getLoc("sluice/" + type + "_sluice"));
        singleTexture("block/" + type + "_sluice_front", getLoc("sluice_front"), "0", getLoc("sluice/" + type + "_sluice"));
    }

    private void makeOldSluice(String type) {
        // Loc/Name of file, parent, texturekey
        singleTexture("block/" + type + "_sluice_body", getLoc("sluice_old_body"), "0", getLoc("sluice/" + type + "_sluice"));
        singleTexture("block/" + type + "_sluice_front", getLoc("sluice_old_front"), "0", getLoc("sluice/" + type + "_sluice"));
    }
}
