package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FusingMachineRecipe extends BaseRecipe<FusingMachineRecipe> {
    public static final MapCodec<FusingMachineRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(FusingMachineRecipe::getInputs),
            FluidStackTemplate.CODEC.fieldOf("result").forGetter(FusingMachineRecipe::getFluidResult),
            EnergyRequirement.CODEC.fieldOf("energy").forGetter(FusingMachineRecipe::getEnergyComponent)
    ).apply(builder, FusingMachineRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FusingMachineRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FusingMachineRecipe::getInputs,
            FluidStackTemplate.STREAM_CODEC, FusingMachineRecipe::getFluidResult,
            EnergyRequirement.STREAM_CODEC, FusingMachineRecipe::getEnergyComponent,
            FusingMachineRecipe::new
    );

    public static final RecipeSerializer<FusingMachineRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final List<Ingredient> inputs;
    private final FluidStackTemplate fluidResult;
    private final EnergyRequirement energyRequirement;

    public FusingMachineRecipe(List<Ingredient> inputs, FluidStackTemplate fluidResult, EnergyRequirement energyRequirement) {
        super(RecipesRegistry.FUSING_MACHINE_SERIALIZER, RecipesRegistry.FUSING_MACHINE_TYPE);

        this.inputs = inputs;
        this.fluidResult = fluidResult;
        this.energyRequirement = energyRequirement;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public FluidStackTemplate getFluidResult() {
        return fluidResult;
    }

    public FluidStack createResult() {
        return fluidResult.create();
    }

    public EnergyRequirement getEnergyComponent() {
        return energyRequirement;
    }

    public boolean test(ResourceHandler<ItemResource> itemHandler) {
        Set<Ingredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(getInputs());

        int found = 0;
        for (int i = 0; i < itemHandler.size(); i++) {
            if (!itemHandler.getResource(i).isEmpty()) {
                Iterator<Ingredient> iter = inputSet.iterator();
                while (iter.hasNext()) {
                    Ingredient ingr = iter.next();
                    if (ingr.test(itemHandler.getResource(i).toStack())) {
                        iter.remove();
                        found++;
                        break;
                    }
                }
                if (found == getInputs().size()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public RecipeSerializer<? extends Recipe<NoInventory>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<NoInventory>> getType() {
        return RecipesRegistry.FUSING_MACHINE_TYPE.get();
    }
}
