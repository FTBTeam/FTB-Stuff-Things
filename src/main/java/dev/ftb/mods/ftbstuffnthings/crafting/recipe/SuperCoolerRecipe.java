package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SuperCoolerRecipe extends BaseRecipe<SuperCoolerRecipe> {
    private final List<Ingredient> inputs;
    private final EnergyRequirement energyRequirement;
    private final SizedFluidIngredient fluidInput;
    private final ItemStack result;

    public SuperCoolerRecipe(List<Ingredient> inputs, SizedFluidIngredient fluidInput, EnergyRequirement energyRequirement, ItemStack result) {
        super(RecipesRegistry.SUPER_COOLER_SERIALIZER, RecipesRegistry.SUPER_COOLER_TYPE);

        this.inputs = inputs;
        this.fluidInput = fluidInput;
        this.energyRequirement = energyRequirement;
        this.result = result;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public SizedFluidIngredient getFluidInput() {
        return fluidInput;
    }

    public EnergyRequirement getEnergyComponent() {
        return energyRequirement;
    }

    public ItemStack getResult() {
        return result;
    }

    public interface IFactory<T extends SuperCoolerRecipe> {
        T create(List<Ingredient> ingredients, SizedFluidIngredient fluidIngredient, EnergyRequirement energyRequirement, ItemStack result);
    }

    public boolean test(IItemHandler itemHandler, FluidStack fluidStack) {
        // note: just testing for a fluid match, not the amount here
        if (!getFluidInput().ingredient().test(fluidStack)) {
            return false;
        }

        Set<Ingredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(getInputs());

        int found = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                Iterator<Ingredient> iter = inputSet.iterator();
                while (iter.hasNext()) {
                    Ingredient ingr = iter.next();
                    if (ingr.test(itemHandler.getStackInSlot(i))) {
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

    public static class Serializer<T extends SuperCoolerRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(SuperCoolerRecipe.IFactory<T> factory) {
            codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Ingredient.CODEC_NONEMPTY.listOf().fieldOf("inputs").forGetter(SuperCoolerRecipe::getInputs),
                    SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid").forGetter(SuperCoolerRecipe::getFluidInput),
                    EnergyRequirement.CODEC.fieldOf("energy").forGetter(SuperCoolerRecipe::getEnergyComponent),
                    ItemStack.CODEC.fieldOf("result").forGetter(SuperCoolerRecipe::getResult)
            ).apply(builder, factory::create));

            streamCodec = StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), SuperCoolerRecipe::getInputs,
                    SizedFluidIngredient.STREAM_CODEC, SuperCoolerRecipe::getFluidInput,
                    EnergyRequirement.STREAM_CODEC, SuperCoolerRecipe::getEnergyComponent,
                    ItemStack.STREAM_CODEC, SuperCoolerRecipe::getResult,
                    factory::create
            );
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
