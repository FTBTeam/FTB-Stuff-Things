package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ItemTagsGenerator extends ItemTagsProvider {
    public ItemTagsGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, FTBStuffNThings.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        appendToTag(Tags.Items.INGOTS, FTBStuffTags.Items.INGOTS_CAST_IRON);
        appendToTag(Tags.Items.NUGGETS, FTBStuffTags.Items.NUGGETS_CAST_IRON);

        addItemsToTag(FTBStuffTags.Items.NUGGETS_CAST_IRON, ItemsRegistry.CAST_IRON_NUGGET);
        addItemsToTag(FTBStuffTags.Items.INGOTS_CAST_IRON, ItemsRegistry.CAST_IRON_INGOT);
        addItemsToTag(FTBStuffTags.Items.GEARS, ItemsRegistry.CAST_IRON_GEAR);

        addItemsToTag(FTBStuffTags.Items.HAMMERS,
                ItemsRegistry.STONE_HAMMER, ItemsRegistry.IRON_HAMMER, ItemsRegistry.GOLD_HAMMER,
                ItemsRegistry.DIAMOND_HAMMER, ItemsRegistry.NETHERITE_HAMMER
        );

        addItemsToTag(FTBStuffTags.Items.CROOKS, ItemsRegistry.CROOK);

        addItemsToTag(FTBStuffTags.Items.MESHES,
                ItemsRegistry.CLOTH_MESH, ItemsRegistry.IRON_MESH, ItemsRegistry.GOLD_MESH,
                ItemsRegistry.DIAMOND_MESH, ItemsRegistry.BLAZING_MESH
        );

        addItemsToTag(FTBStuffTags.Items.WOODEN_SLUICES,
                ItemsRegistry.OAK_SLUICE, ItemsRegistry.SPRUCE_SLUICE, ItemsRegistry.BIRCH_SLUICE,
                ItemsRegistry.JUNGLE_SLUICE, ItemsRegistry.ACACIA_SLUICE, ItemsRegistry.DARK_OAK_SLUICE,
                ItemsRegistry.MANGROVE_SLUICE, ItemsRegistry.CHERRY_SLUICE, ItemsRegistry.PALE_OAK_SLUICE,
                ItemsRegistry.CRIMSON_SLUICE, ItemsRegistry.WARPED_SLUICE, ItemsRegistry.BAMBOO_SLUICE
        );
    }

    @SafeVarargs
    private void addItemsToTag(TagKey<Item> tag, Supplier<? extends ItemLike>... items) {
        tag(tag).add(Arrays.stream(items).map(Supplier::get).map(ItemLike::asItem).toArray(Item[]::new));
    }

    @SafeVarargs
    private void appendToTag(TagKey<Item> tag, TagKey<Item>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @Override
    public String getName() {
        return FTBStuffNThings.MODID + " Item Tags";
    }
}
