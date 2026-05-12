package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.data.PackOutput;

import java.util.Optional;

public class ModelsGenerator extends ModelProvider {
    public static final ModelTemplate SLUICE_TEMPLATE = new ModelTemplate(
            Optional.of(ModelLocationUtils.decorateBlockModelLocation(FTBStuffNThings.MOD_ID + ":sluice_body")),
            Optional.empty(),
            TextureSlot.ALL
    );

    public ModelsGenerator(PackOutput output) {
        super(output, FTBStuffNThings.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        BlocksRegistry.ALL_SLUICES.forEach(block -> {
            String suffix = "_" + block.get().getSluiceType().getSerializedName();
            SLUICE_TEMPLATE.createWithSuffix(block.get(),
                    suffix,
                    new TextureMapping().put(TextureSlot.ALL, TextureMapping.getBlockTexture(block.get(), suffix)),
                    blockModels.modelOutput);
        });
    }
}
