package dev.ftb.mods.ftbstuffnthings.data;


import com.google.gson.JsonElement;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        makeMCSluice("spruce");
        makeMCSluice("birch");
        makeMCSluice("jungle");
        makeMCSluice("acacia");
        makeSluice("dark_oak");
        makeMCSluice("mangrove");
        makeMCSluice("cherry");
        makeSluice("pale_oak");
        makeMCSluice("crimson");
        makeMCSluice("warped");

        makeSluice("iron");
        makeSluice("diamond");
        makeSluice("netherite");
    }

    private void makeSluice(String type) {
        // Loc/Name of file, parent, texturekey
        singleTexture("block/" + type + "_sluice_body", getLoc("sluice_body"), "0", getLoc("sluice/" + type + "_sluice"));
        singleTexture("block/" + type + "_sluice_front", getLoc("sluice_front"), "0", getLoc("sluice/" + type + "_sluice"));
    }

    private void makeMCSluice(String type) {
        // Loc/Name of file, parent, texturekey
        singleTexture("block/" + type + "_sluice_body", getLoc("sluice_body"), "0", getMCLoc(type + "_planks"));
        singleTexture("block/" + type + "_sluice_front", getLoc("sluice_front"), "0", getMCLoc(type + "_planks"));
    }
}
