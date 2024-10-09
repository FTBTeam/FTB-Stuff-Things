package dev.ftb.mods.ftbobb.recipes;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbobb.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ItemWithChance(ItemStack item, double chance) {
	public static final Codec<ItemWithChance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			ItemStack.CODEC.fieldOf("item").forGetter(ItemWithChance::item),
			Codec.DOUBLE.validate(MiscUtil::validateChanceRange).fieldOf("chance").forGetter(ItemWithChance::chance)
	).apply(builder, ItemWithChance::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemWithChance> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, ItemWithChance::item,
			ByteBufCodecs.DOUBLE, ItemWithChance::chance,
			ItemWithChance::new
	);

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("item", item)
			.add("chance", chance)
			.toString();
	}

	public ItemWithChance copy(){
		return new ItemWithChance(item.copy(), chance);
	}
}
