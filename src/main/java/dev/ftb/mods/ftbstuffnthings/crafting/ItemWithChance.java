package dev.ftb.mods.ftbstuffnthings.crafting;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemWithChance(ItemStackTemplate item, double chance) {
	public static final Codec<ItemWithChance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			ItemStackTemplate.CODEC.fieldOf("item").forGetter(ItemWithChance::item),
			Codec.DOUBLE.validate(MiscUtil::validateChanceRange).fieldOf("chance").forGetter(ItemWithChance::chance)
	).apply(builder, ItemWithChance::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemWithChance> STREAM_CODEC = StreamCodec.composite(
			ItemStackTemplate.STREAM_CODEC, ItemWithChance::item,
			ByteBufCodecs.DOUBLE, ItemWithChance::chance,
			ItemWithChance::new
	);

	public static ItemWithChance create(ItemStack stack, double chance) {
		return new ItemWithChance(ItemStackTemplate.fromNonEmptyStack(stack), chance);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("item", item)
			.add("chance", chance)
			.toString();
	}
}
