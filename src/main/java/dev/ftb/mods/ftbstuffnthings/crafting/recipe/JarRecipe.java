package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.integration.stages.StageHelper;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class JarRecipe extends BaseRecipe<JarRecipe> implements Comparable<JarRecipe> {
	private static final MapCodec<JarRecipe> RAW_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			SizedIngredient.NESTED_CODEC.listOf(0, 3).optionalFieldOf("input_items", List.of())
					.forGetter(JarRecipe::getInputItems),
			SizedFluidIngredient.CODEC.listOf(0, 3).optionalFieldOf("input_fluids", List.of())
					.forGetter(JarRecipe::getInputFluids),
			ItemStackTemplate.CODEC.listOf(0, 3).optionalFieldOf("output_items", List.of())
					.forGetter(JarRecipe::getOutputItems),
			FluidStackTemplate.CODEC.listOf(0, 3).optionalFieldOf("output_fluids", List.of())
					.forGetter(JarRecipe::getOutputFluids),
			StringRepresentable.fromEnum(Temperature::values).optionalFieldOf("temperature", Temperature.NORMAL)
					.forGetter(JarRecipe::getTemperature),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("time", 200)
					.forGetter(JarRecipe::getTime),
			Codec.BOOL.optionalFieldOf("can_repeat", true)
					.forGetter(JarRecipe::canRepeat),
			Codec.STRING.optionalFieldOf("stage", "")
					.forGetter(JarRecipe::getStage)
	).apply(builder, JarRecipe::new));
	public static final MapCodec<JarRecipe> CODEC = RAW_CODEC.validate(JarRecipe::validateRecipe);

	public static final StreamCodec<RegistryFriendlyByteBuf, JarRecipe> STREAM_CODEC = StreamCodec.composite(
			SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getInputItems,
			SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getInputFluids,
			ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getOutputItems,
			FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), JarRecipe::getOutputFluids,
			NeoForgeStreamCodecs.enumCodec(Temperature.class), JarRecipe::getTemperature,
			ByteBufCodecs.VAR_INT, JarRecipe::getTime,
			ByteBufCodecs.BOOL, JarRecipe::canRepeat,
			ByteBufCodecs.STRING_UTF8, JarRecipe::getStage,
			JarRecipe::new
	);

	public static final RecipeSerializer<JarRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final Temperature temperature;
	private final int time;
	private final List<SizedIngredient> inputItems;
	private final List<SizedFluidIngredient> inputFluids;
	private final List<ItemStackTemplate> outputItems;
	private final List<FluidStackTemplate> outputFluids;
	private final boolean canRepeat;
	private final String stage;
	private final Lazy<String> filterText = Lazy.of(this::buildFilterText);

	public JarRecipe(List<SizedIngredient> inputItems, List<SizedFluidIngredient> inputFluids,
	                 List<ItemStackTemplate> outputItems, List<FluidStackTemplate> outputFluids,
	                 Temperature temperature, int time, boolean canRepeat, String stage)
	{
		super(RecipesRegistry.TEMPERED_JAR_SERIALIZER, RecipesRegistry.TEMPERED_JAR_TYPE);

		this.inputItems = inputItems;
		this.inputFluids = inputFluids;
		this.outputItems = outputItems;
		this.outputFluids = outputFluids;
		this.temperature = temperature;
		this.time = time;
		this.canRepeat = canRepeat;
		this.stage = stage;
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

	public List<ItemStackTemplate> getOutputItems() {
		return outputItems;
	}

	public List<FluidStackTemplate> getOutputFluids() {
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

		for (ItemStackTemplate stack : outputItems) {
			set.add(stack.create().getHoverName().getString().trim().toLowerCase());
		}

		for (FluidStackTemplate stack : outputFluids) {
			set.add(stack.create().getHoverName().getString().trim().toLowerCase());
		}

		for (SizedIngredient ingredient : inputItems) {
			ingredient.ingredient().getValues().forEach(holder -> {
				set.add(holder.value().getDefaultInstance().getHoverName().getString().trim().toLowerCase());
			});
		}

		for (SizedFluidIngredient ingredient : inputFluids) {
			ingredient.ingredient().fluids().forEach(holder -> {
				set.add(new FluidStack(holder.value(), 1000).getHoverName().getString().trim().toLowerCase());
			});
		}

		return String.join(" ", set);
	}

	public List<Either<SizedFluidIngredient,SizedIngredient>> allInputs() {
		List<Either<SizedFluidIngredient,SizedIngredient>> res = new ArrayList<>();
		inputFluids.forEach(f -> res.add(Either.left(f)));
		inputItems.forEach(i -> res.add(Either.right(i)));
		return res;
	}

	/**
	 * Test if the given item and fluids match this recipe, optionally taking item/fluid amounts into consideration.
	 *
	 * @param jarTemperature	the current jar temperature
	 * @param jarItems			the items to test
	 * @param jarFluids			the fluids to test
	 * @param checkAmounts		true to check ingredient amounts too, false to just check for the right items/fluids
	 * @return true if the recipe matches, false otherwise
	 */
	public boolean test(Temperature jarTemperature, ResourceHandler<ItemResource> jarItems, ResourceHandler<FluidResource> jarFluids, boolean checkAmounts) {
		if (jarTemperature != getTemperature()) {
			return false;
		}

		int matched = 0;
		for (SizedIngredient inputItem : inputItems) {
			for (int i = 0; i < jarItems.size(); i++) {
				ItemStack toTest = jarItems.getResource(i).toStack(jarItems.getAmountAsInt(i));
				if (checkAmounts ? inputItem.test(toTest) : inputItem.ingredient().test(toTest)) {
					matched++;
					break;
				}
			}
		}
		if (matched != inputItems.size()) return false;

		matched = 0;
		for (SizedFluidIngredient inputFluid : inputFluids) {
			for (int i = 0; i < jarFluids.size(); i++) {
				FluidStack toTest = jarFluids.getResource(i).toStack(jarFluids.getAmountAsInt(i));
				if (checkAmounts ? inputFluid.test(toTest) : inputFluid.ingredient().test(toTest)) {
					matched++;
					break;
				}
			}
		}
		return matched == inputFluids.size();
	}

	public int inputIngredientCount() {
		return inputFluids.size() + inputItems.size();
	}

	@Override
	public int compareTo(JarRecipe o) {
		// compare by temperature, then by number of input ingredients, then by total item count, then by total fluid count
		// the largest number and/or count of ingredients sorts first

		int c = getTemperature().compareTo(o.getTemperature());
		if (c != 0) return c;

		c = Integer.compare(o.inputIngredientCount(), inputIngredientCount());
		if (c != 0) return c;

		c = Integer.compare(
				o.getInputItems().stream().mapToInt(SizedIngredient::count).sum(),
				getInputItems().stream().mapToInt(SizedIngredient::count).sum()
		);
		if (c != 0) return c;

		return Integer.compare(
				o.getInputFluids().stream().mapToInt(SizedFluidIngredient::amount).sum(),
				getInputFluids().stream().mapToInt(SizedFluidIngredient::amount).sum()
		);
	}

	private static <T extends JarRecipe> DataResult<T> validateRecipe(T recipe) {
		if (recipe.getInputItems().isEmpty() && recipe.getInputFluids().isEmpty()) {
			return DataResult.error(() -> "at least one of input_items & input_fluids must be non-empty!");
		}
		if (recipe.getOutputItems().isEmpty() && recipe.getOutputFluids().isEmpty()) {
			return DataResult.error(() -> "at least one of output_items & output_fluids must be non-empty!");
		}
		if (recipe.inputIngredientCount() > 3) {
			return DataResult.error(() -> "must be 1-3 item & fluid inputs combined!");
		}
		if (recipe.getOutputItems().size() + recipe.getOutputFluids().size() > 3) {
			return DataResult.error(() -> "must be 1-3 item & fluid outputs combined!");
		}
		return DataResult.success(recipe);
	}
}