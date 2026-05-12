package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SluiceRecipe extends BaseRecipe<SluiceRecipe> {
    public static final MapCodec<SluiceRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Ingredient.CODEC.fieldOf("input").forGetter(SluiceRecipe::getIngredient),
            ItemWithChance.CODEC.listOf().fieldOf("results").forGetter(SluiceRecipe::getResults),
            Codec.INT.optionalFieldOf("max_results", 4).forGetter(SluiceRecipe::getMaxResults),
            SizedFluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(SluiceRecipe::getFluid),
            Codec.FLOAT.optionalFieldOf("processing_time_multiplier", 1F).forGetter(SluiceRecipe::getProcessingTimeMultiplier),
            MeshType.CODEC.listOf().fieldOf("mesh_types").forGetter(SluiceRecipe::getMeshTypesAsList)
    ).apply(builder, SluiceRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SluiceRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, SluiceRecipe::getIngredient,
            ItemWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()), SluiceRecipe::getResults,
            ByteBufCodecs.VAR_INT, SluiceRecipe::getMaxResults,
            ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC), SluiceRecipe::getFluid,
            ByteBufCodecs.FLOAT, SluiceRecipe::getProcessingTimeMultiplier,
            MeshType.STREAM_CODEC.apply(ByteBufCodecs.list()), SluiceRecipe::getMeshTypesAsList,
            SluiceRecipe::new
    );

    public static final RecipeSerializer<SluiceRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final Ingredient ingredient;
    private final List<ItemWithChance> results;
    private final int maxResults;
    private final Optional<SizedFluidIngredient> fluid;
    private final float processingTimeMultiplier;
    private final Set<MeshType> meshTypes;

    public SluiceRecipe(Ingredient ingredient, List<ItemWithChance> results, int maxResults, Optional<SizedFluidIngredient> fluid, float processingTimeMultiplier, List<MeshType> meshTypes) {
        super(RecipesRegistry.SLUICE_SERIALIZER, RecipesRegistry.SLUICE_TYPE);

        this.ingredient = ingredient;
        this.results = results;
        this.maxResults = maxResults;
        this.fluid = fluid;
        this.processingTimeMultiplier = processingTimeMultiplier;
        this.meshTypes = EnumSet.copyOf(meshTypes);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public List<ItemWithChance> getResults() {
        return results;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public Optional<SizedFluidIngredient> getFluid() {
        return fluid;
    }

    public boolean testFluid(FluidStack toCheck, boolean checkAmount, double fluidModifier) {
        return fluid.map(ingr -> checkAmount ?
                ingr.test(toCheck.copyWithAmount((int) (toCheck.getAmount() / fluidModifier))) :
                ingr.ingredient().test(toCheck)
        ).orElse(true);
    }

    public boolean testFluid(FluidStack toCheck, boolean checkAmount) {
        return testFluid(toCheck, checkAmount, 1.0);
    }

    public float getProcessingTimeMultiplier() {
        return processingTimeMultiplier;
    }

    public Set<MeshType> getMeshTypes() {
        return Collections.unmodifiableSet(meshTypes);
    }

    public List<MeshType> getMeshTypesAsList() {
        return List.copyOf(meshTypes);
    }
}
