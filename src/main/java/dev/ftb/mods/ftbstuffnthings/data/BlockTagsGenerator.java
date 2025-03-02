package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class BlockTagsGenerator extends BlockTagsProvider {
    private static final Pattern SHOVEL_BLOCKS = Pattern.compile("(clay|dirt|dust|gravel|sand|soil)");

    public BlockTagsGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, FTBStuffNThings.MODID, existingFileHelper);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FTBStuffTags.Blocks.MINEABLE_WITH_HAMMER).addTags(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL);

        BlocksRegistry.BLOCKS.getEntries().forEach(entry -> {
            Block block = entry.get();
            String path = entry.getId().getPath();
            if (path.startsWith("compressed_")) {
                TagKey<Block> toolTag = SHOVEL_BLOCKS.matcher(path).find() ?
                        BlockTags.MINEABLE_WITH_SHOVEL : BlockTags.MINEABLE_WITH_PICKAXE;
                tag(toolTag).add(block);
                tag(BlockTags.NEEDS_STONE_TOOL).add(block);
            } else if (!(block instanceof LiquidBlock) && !(block instanceof AirBlock)) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
                tag(BlockTags.NEEDS_STONE_TOOL).add(block);
            }
        });

        BlocksRegistry.ALL_SLUICES.forEach(sluice -> tag(FTBStuffTags.Blocks.SLUICE).add(sluice.get()));
        BlocksRegistry.WOODEN_SLUICE.forEach(sluice -> tag(FTBStuffTags.Blocks.WOODEN_SLUICES).add(sluice.get()));
        BlocksRegistry.CRATES.forEach(crate -> tag(FTBStuffTags.Blocks.CRATE).add(crate.get()));
        BlocksRegistry.BARRELS.forEach(barrel -> tag(FTBStuffTags.Blocks.BARREL).add(barrel.get()));
        BlocksRegistry.waterStrainers().forEach(strainer -> tag(FTBStuffTags.Blocks.WATER_STRAINER).add(strainer.get()));

        MeshType.NON_EMPTY_VALUES.forEach(mesh -> {
            if (mesh == MeshType.BLAZING) {
                tag(FTBStuffTags.Blocks.allowedMeshes(mesh)).add(BlocksRegistry.NETHERITE_SLUICE.get());
            } else {
                tag(FTBStuffTags.Blocks.allowedMeshes(mesh)).addTag(FTBStuffTags.Blocks.SLUICE);
            }
        });
    }
}
