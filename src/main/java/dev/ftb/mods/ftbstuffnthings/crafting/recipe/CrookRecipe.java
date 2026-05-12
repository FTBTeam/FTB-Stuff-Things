package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class CrookRecipe extends BaseRecipe<CrookRecipe> {
    public static final MapCodec<CrookRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Ingredient.CODEC.fieldOf("input").forGetter(CrookRecipe::getIngredient),
            ItemWithChance.CODEC.listOf().fieldOf("results").forGetter(CrookRecipe::getResults),
            Codec.INT.optionalFieldOf("max", 0).forGetter(CrookRecipe::getMax),
            Codec.BOOL.optionalFieldOf("replace_drops", true).forGetter(CrookRecipe::replaceDrops)
    ).apply(builder, CrookRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CrookRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CrookRecipe::getIngredient,
            ItemWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()), CrookRecipe::getResults,
            ByteBufCodecs.VAR_INT, CrookRecipe::getMax,
            ByteBufCodecs.BOOL, CrookRecipe::replaceDrops,
            CrookRecipe::new
    );

    public static final RecipeSerializer<CrookRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final Ingredient ingredient;
    private final List<ItemWithChance> results;
    private final int max;
    private final boolean replaceDrops;

    public CrookRecipe(Ingredient ingredient, List<ItemWithChance> results, int max, boolean replaceDrops) {
        super(RecipesRegistry.CROOK_SERIALIZER, RecipesRegistry.CROOK_TYPE);

        this.ingredient = ingredient;
        this.results = results;
        this.max = max;
        this.replaceDrops = replaceDrops;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public List<ItemWithChance> getResults() {
        return results;
    }

    public int getMax() {
        return max;
    }

    public boolean replaceDrops() {
        return replaceDrops;
    }

    public record CrookDrops(List<ItemWithChance> items, int max, boolean replaceDrops) {
    }
}
