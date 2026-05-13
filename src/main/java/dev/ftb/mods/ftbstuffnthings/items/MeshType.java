package dev.ftb.mods.ftbstuffnthings.items;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum MeshType implements StringRepresentable {
    EMPTY("empty", null, null),

    CLOTH("cloth", ItemsRegistry.CLOTH_MESH, Tags.Items.STRINGS),
    IRON("iron", ItemsRegistry.IRON_MESH, Tags.Items.INGOTS_IRON),
    GOLD("gold", ItemsRegistry.GOLD_MESH, Tags.Items.INGOTS_GOLD),
    DIAMOND("diamond", ItemsRegistry.DIAMOND_MESH, Tags.Items.GEMS_DIAMOND),
    BLAZING("blazing", ItemsRegistry.BLAZING_MESH, Tags.Items.RODS_BLAZE);

    public static final Codec<MeshType> CODEC = StringRepresentable.fromEnum(MeshType::values);
    public static final StreamCodec<ByteBuf, MeshType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static final List<MeshType> NON_EMPTY_VALUES = Arrays.stream(values()).filter(e -> e != EMPTY).toList();

    @Nullable
    private final DeferredItem<MeshItem> meshItem;
    private final String name;
    @Nullable
    private final TagKey<Item> ingredientTag;

    MeshType(String name, @Nullable DeferredItem<MeshItem> meshItem, @Nullable TagKey<Item> ingredientTag) {
        this.name = name;
        this.meshItem = meshItem;
        this.ingredientTag = ingredientTag;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public ItemStack createItemStack() {
        return meshItem == null ? ItemStack.EMPTY : meshItem.toStack();
    }

    public Item asItem() {
        return meshItem == null ? Items.AIR : meshItem.get();
    }

    public @Nullable TagKey<Item> getIngredientTag() {
        return ingredientTag;
    }
}
