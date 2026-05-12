package dev.ftb.mods.ftbstuffnthings.temperature;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Temperature implements StringRepresentable {
	NORMAL("normal", new DustParticleOptions(0xFFE0E0E0, 1F), 0.1F, ChatFormatting.GRAY),
	HOT("hot", ParticleTypes.FLAME, 0.1F, ChatFormatting.GOLD),
	SUPERHEATED("superheated", ParticleTypes.SOUL_FIRE_FLAME, 0.1F, ChatFormatting.AQUA),
	CHILLED("chilled", ParticleTypes.END_ROD, 0.3F, ChatFormatting.BLUE);

	public static final Temperature[] VALUES = values();
	public static final Map<String, Temperature> MAP = Arrays.stream(VALUES).collect(Collectors.toMap(t -> t.id, t -> t));

	private final String id;
	private final Component name;
	private final Identifier texture;
	private final Icon<?> icon;
	private final ParticleOptions particleOptions;
	private final float particleYOffset;
	private final ChatFormatting color;

	Temperature(String id, ParticleOptions particleOptions, float particleYOffset, ChatFormatting color) {
		this.id = id;
		this.particleOptions = particleOptions;
		this.particleYOffset = particleYOffset;
		this.color = color;

		name = Component.translatable(FTBStuffNThings.MOD_ID + ".temperature." + this.id);
		texture = FTBStuffNThings.id("textures/gui/temperature/" + this.id + ".png");
		icon = Icon.getIcon(texture);
	}

	@Override
	public String getSerializedName() {
		return id;
	}

	public Component getName() {
		return name.copy().withStyle(color);
	}

	public Identifier getTexture() {
		return texture;
	}

	public Icon<?> getIcon() {
		return icon;
	}

	public static Temperature byName(String name) {
		return MAP.getOrDefault(name.toLowerCase(), NORMAL);
	}

	public ParticleOptions getParticleOptions() {
		return particleOptions;
	}

	public float getParticleYOffset() {
		return particleYOffset;
	}
}