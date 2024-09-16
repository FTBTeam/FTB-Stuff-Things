package dev.ftb.mods.ftbobb.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbobb.integration.stages.StageHelper;
import dev.ftb.mods.ftbobb.registry.RecipesRegistry;
import dev.ftb.mods.ftbobb.temperature.Temperature;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public class JarRecipe implements Recipe<NoInventory> {
	public static final Comparator<JarRecipe> COMPARATOR = Comparator
			.comparingInt(JarRecipe::getTempOrder)
			.thenComparing(JarRecipe::getFilterText);

	private final Temperature temperature;
	private final int time;
	private final List<SizedIngredient> inputItems;
	private final List<SizedFluidIngredient> inputFluids;
	private final List<ItemStack> outputItems;
	private final List<FluidStack> outputFluids;
	private final boolean canRepeat;
	private final String stage;
	private final Lazy<String> filterText = Lazy.of(this::buildFilterText);

	public JarRecipe(List<SizedIngredient> inputItems, List<SizedFluidIngredient> inputFluids,
					 List<ItemStack> outputItems, List<FluidStack> outputFluids,
					 Temperature temperature, int time, boolean canRepeat, String stage)
	{
		this.inputItems = inputItems;
		this.inputFluids = inputFluids;
		this.outputItems = outputItems;
		this.outputFluids = outputFluids;
		this.temperature = temperature;
		this.time = time;
		this.canRepeat = canRepeat;
		this.stage = stage;
	}

	@Override
	public boolean matches(NoInventory inv, Level world) {
		return true;
	}

	@Override
	public ItemStack assemble(NoInventory noInventory, HolderLookup.Provider provider) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipesRegistry.JAR_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipesRegistry.JAR_TYPE.get();
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public int getTime() {
		return time;
	}

	public List<SizedIngredient> getInputItems() {
		return inputItems;
	}

	public List<SizedFluidIngredient> getInputFluids() {
		return inputFluids;
	}

	public List<ItemStack> getOutputItems() {
		return outputItems;
	}

	public List<FluidStack> getOutputFluids() {
		return outputFluids;
	}

	public boolean canRepeat() {
		return canRepeat;
	}

	public String getStage() {
		return stage;
	}

	public boolean isAvailableFor(Player player) {
		return stage.isEmpty() || StageHelper.hasStage(player, stage);
	}

	public boolean hasItems() {
		return !inputItems.isEmpty() || !outputItems.isEmpty();
	}

	public boolean hasFluids() {
		return !inputFluids.isEmpty() || !outputFluids.isEmpty();
	}

	private int getTempOrder() {
		return temperature.ordinal();
	}

	public String getFilterText() {
		return filterText.get();
	}

	private String buildFilterText() {
		LinkedHashSet<String> set = new LinkedHashSet<>();

		for (ItemStack stack : outputItems) {
			set.add(stack.getHoverName().getString().trim().toLowerCase());
		}

		for (FluidStack stack : outputFluids) {
			set.add(stack.getHoverName().getString().trim().toLowerCase());
		}

		for (SizedIngredient ingredient : inputItems) {
			for (ItemStack stack : ingredient.ingredient().getItems()) {
				set.add(stack.getHoverName().getString().trim().toLowerCase());
			}
		}

		for (SizedFluidIngredient ingredient : inputFluids) {
			for (FluidStack stack : ingredient.ingredient().getStacks()) {
				set.add(stack.getHoverName().getString().trim().toLowerCase());
			}
		}

		return String.join(" ", set);
	}

	public interface IFactory<T extends JarRecipe> {
		T create(List<SizedIngredient> inputItems, List<SizedFluidIngredient> inputFluids, List<ItemStack> outputItems, List<FluidStack> outputFluids, Temperature temperature, int time, boolean canRepeat, String stage);
	}

	public static class Serializer<T extends JarRecipe> implements RecipeSerializer<T> {
		private final MapCodec<T> codec;
		private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

		public Serializer(IFactory<T> factory) {
			codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
					SizedIngredient.FLAT_CODEC.listOf().fieldOf("input_items")
							.forGetter(JarRecipe::getInputItems),
					SizedFluidIngredient.FLAT_CODEC.listOf().fieldOf("input_fluids")
							.forGetter(JarRecipe::getInputFluids),
					ItemStack.CODEC.listOf().fieldOf("output_items")
							.forGetter(JarRecipe::getOutputItems),
					FluidStack.CODEC.listOf().fieldOf("output_fluids")
							.forGetter(JarRecipe::getOutputFluids),
					StringRepresentable.fromEnum(Temperature::values).optionalFieldOf("temperature", Temperature.NONE)
							.forGetter(JarRecipe::getTemperature),
					Codec.INT.optionalFieldOf("time", 200)
							.forGetter(JarRecipe::getTime),
					Codec.BOOL.optionalFieldOf("can_repeat", true)
							.forGetter(JarRecipe::canRepeat),
					Codec.STRING.optionalFieldOf("stage", "")
							.forGetter(JarRecipe::getStage)
			).apply(builder, factory::create));

			streamCodec = NetworkHelper.composite(
					SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getInputItems,
					SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getInputFluids,
					ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getOutputItems,
					FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getOutputFluids,
					NeoForgeStreamCodecs.enumCodec(Temperature.class), JarRecipe::getTemperature,
					ByteBufCodecs.VAR_INT, JarRecipe::getTime,
					ByteBufCodecs.BOOL, JarRecipe::canRepeat,
					ByteBufCodecs.STRING_UTF8, JarRecipe::getStage,
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