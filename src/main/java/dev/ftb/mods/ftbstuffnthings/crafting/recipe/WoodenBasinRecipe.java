package dev.ftb.mods.ftbstuffnthings.crafting.recipe;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.crafting.BaseRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WoodenBasinRecipe extends BaseRecipe<WoodenBasinRecipe> {
    public static final MapCodec<WoodenBasinRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("input").forGetter(WoodenBasinRecipe::getInputStateStr),
            FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(WoodenBasinRecipe::getFluidResult),
            Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(WoodenBasinRecipe::getProductionChance),
            Codec.FLOAT.optionalFieldOf("block_consume_chance", 1f).forGetter(WoodenBasinRecipe::getBlockConsumeChance),
            Codec.BOOL.optionalFieldOf("drop_items", false).forGetter(WoodenBasinRecipe::dropItems)
    ).apply(builder, WoodenBasinRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WoodenBasinRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WoodenBasinRecipe::getInputStateStr,
            FluidStackTemplate.STREAM_CODEC, WoodenBasinRecipe::getFluidResult,
            ByteBufCodecs.FLOAT, WoodenBasinRecipe::getProductionChance,
            ByteBufCodecs.FLOAT, WoodenBasinRecipe::getBlockConsumeChance,
            ByteBufCodecs.BOOL, WoodenBasinRecipe::dropItems,
            WoodenBasinRecipe::new
    );

    public static final RecipeSerializer<WoodenBasinRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final String inputStateStr;
    private final FluidStackTemplate fluidResult;
    private final float productionChance;
    private final float blockConsumeChance;
    private final boolean dropItems;
    private final Lazy<BlockPredicateArgument.Result> inputPredicate;

    public WoodenBasinRecipe(String inputStateStr, FluidStackTemplate fluidResult, float productionChance, float blockConsumeChance, boolean dropItems) {
        super(RecipesRegistry.WOODEN_BASIN_SERIALIZER, RecipesRegistry.WOODEN_BASIN_TYPE);

        this.inputStateStr = inputStateStr;
        this.fluidResult = fluidResult;
        this.productionChance = productionChance;
        this.blockConsumeChance = blockConsumeChance;
        this.dropItems = dropItems;

        inputPredicate = Lazy.of(() -> {
            // not ideal, but data generation chokes on block tags if they're parsed here
            try {
                return BlockPredicateArgument.parse(BuiltInRegistries.BLOCK, new StringReader(inputStateStr));
            } catch (CommandSyntaxException e) {
                return new BadResult();
            }
        });
    }

    private Set<Block> getInputBlocks() {
        if (inputPredicate.get() instanceof BlockPredicateArgument.BlockPredicate b) {
            return Set.of(b.state.getBlock());
        } else if (inputPredicate.get() instanceof BlockPredicateArgument.TagPredicate t) {
            return t.tag.stream().map(Holder::value).collect(Collectors.toSet());
        }
        return Set.of();
    }

    public List<Either<ItemStack, Fluid>> getInputsForDisplay() {
        // for JEI purposes
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

    public String getInputStateStr() {
        return inputStateStr;
    }

    public float getProductionChance() {
        return productionChance;
    }

    public float getBlockConsumeChance() {
        return blockConsumeChance;
    }

    public FluidStackTemplate getFluidResult() {
        return fluidResult;
    }

    public boolean testInput(BlockInWorld state) {
        return inputPredicate.get().test(state);
    }

    public boolean dropItems() {
        return dropItems;
    }

    private static class BadResult implements BlockPredicateArgument.Result {
        @Override
        public boolean test(BlockInWorld blockInWorld) {
            return false;
        }

        @Override
        public boolean requiresNbt() {
            return false;
        }
    }
}
