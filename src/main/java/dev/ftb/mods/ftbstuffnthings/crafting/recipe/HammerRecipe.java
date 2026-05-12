package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class HammerRecipe extends BaseRecipe<HammerRecipe> {
    public static final MapCodec<HammerRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Ingredient.CODEC.fieldOf("input").forGetter(HammerRecipe::getIngredient),
                    ItemStackTemplate.CODEC.listOf().fieldOf("results").forGetter(HammerRecipe::getResults)
            ).apply(builder, HammerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HammerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, HammerRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), HammerRecipe::getResults,
            HammerRecipe::new
    );

    public static final RecipeSerializer<HammerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final Ingredient ingredient;
    private final List<ItemStackTemplate> results;

    public HammerRecipe(Ingredient ingredient, List<ItemStackTemplate> results) {
        super(RecipesRegistry.HAMMER_SERIALIZER, RecipesRegistry.HAMMER_TYPE);

        this.ingredient = ingredient;
        this.results = results;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public List<ItemStackTemplate> getResults() {
        return results;
    }
}
