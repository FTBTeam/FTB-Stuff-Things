package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DripperRecipe extends BaseRecipe<DripperRecipe> {
	public static final MapCodec<DripperRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.STRING.fieldOf("input").forGetter(DripperRecipe::getInputStateStr),
			Codec.STRING.fieldOf("output").forGetter(DripperRecipe::getOutputStateStr),
			FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(DripperRecipe::getFluid),
			Codec.DOUBLE.validate(MiscUtil::validateChanceRange).optionalFieldOf("chance", 1.0).forGetter(DripperRecipe::getChance),
			Codec.BOOL.optionalFieldOf("consume_fluid_on_fail", false).forGetter(DripperRecipe::consumeFluidOnFail)
	).apply(builder, DripperRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, DripperRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, DripperRecipe::getInputStateStr,
			ByteBufCodecs.STRING_UTF8, DripperRecipe::getOutputStateStr,
			FluidStackTemplate.STREAM_CODEC, DripperRecipe::getFluid,
			ByteBufCodecs.DOUBLE, DripperRecipe::getChance,
			ByteBufCodecs.BOOL, DripperRecipe::consumeFluidOnFail,
			DripperRecipe::new
	);

	public static final RecipeSerializer<DripperRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final String inputStateStr;
	private final BlockPredicateArgument.Result inputPredicate;
	private final String outputString;
	private final BlockState outputState;
	private final FluidStackTemplate fluid;
	private final double chance;
	private final boolean consumeFluidOnFail;

	public DripperRecipe(String inputStateStr, String outputStateStr, FluidStackTemplate fluid, double chance, boolean consumeFluidOnFail) {
		super(RecipesRegistry.DRIP_SERIALIZER, RecipesRegistry.DRIP_TYPE);

		this.inputStateStr = inputStateStr;
		this.outputString = outputStateStr;
		this.fluid = fluid;
		this.chance = chance;
		this.consumeFluidOnFail = consumeFluidOnFail;

		try {
			inputPredicate = BlockPredicateArgument.parse(BuiltInRegistries.BLOCK, new StringReader(inputStateStr));
		} catch (CommandSyntaxException e) {
			throw new JsonSyntaxException(e);
		}

		try {
			BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, new StringReader(outputStateStr), false);
			outputState = blockResult.blockState();
		} catch (CommandSyntaxException e) {
			throw new JsonSyntaxException(e);
		}
	}

	public String getInputStateStr() {
		return inputStateStr;
	}

	public String getOutputStateStr() {
		return outputString;
	}

	private Set<Block> getInputBlocks() {
		if (inputPredicate instanceof BlockPredicateArgument.BlockPredicate b) {
			return Set.of(b.state.getBlock());
		} else if (inputPredicate instanceof BlockPredicateArgument.TagPredicate t) {
			return t.tag.stream().map(Holder::value).collect(Collectors.toSet());
		}
		return Set.of();
	}

	public List<Either<ItemStack, Fluid>> getInputsForDisplay() {
		Set<Block> blocks = getInputBlocks();
		List<Either<ItemStack, Fluid>> res = new ArrayList<>();

		for (Block b : blocks) {
			if (b instanceof LiquidBlock l) {
				if (l.fluid != Fluids.EMPTY) {
					res.add(Either.right(l.fluid));
				}
			} else {
				ItemStack s = b.asItem().getDefaultInstance();
				if (!s.isEmpty()) {
					res.add(Either.left(s));
				}
			}
		}
		return res;
	}

	/**
	 * {@return an item if the block has an item or a fluidstack if the block is a fluid block, otherwise empty item stack}
	 */
	public Either<ItemStack, Fluid> getOutputItemOrFluid() {
		Block b = outputState.getBlock();
		return b instanceof LiquidBlock l ?
				Either.right(l.fluid) :
				Either.left(b.asItem().getDefaultInstance());
	}

	public BlockState getOutputState() {
		return outputState;
	}

	public boolean consumeFluidOnFail() {
		return consumeFluidOnFail;
	}

	public FluidStackTemplate getFluid() {
		return fluid;
	}

	public double getChance() {
		return chance;
	}

	public boolean testInput(FluidStack fluidInDripper, Level level, BlockPos pos) {
		// note: just checking for a fluid match; not checking amount here
		return FluidStack.isSameFluidSameComponents(fluidInDripper, fluid)
				&& inputPredicate.test(new BlockInWorld(level, pos, false));
	}
}
