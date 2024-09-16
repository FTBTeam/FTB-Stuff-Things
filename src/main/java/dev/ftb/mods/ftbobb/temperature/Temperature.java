package dev.ftb.mods.ftbobb.temperature;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbobb.FTBOBB;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public enum Temperature implements StringRepresentable {
	NONE("none", new DustParticleOptions(new Vector3f(0.9F, 0.9F, 0.9F), 1F), 0.1F),
	LOW("low", ParticleTypes.FLAME, 0.1F),
	HIGH("high", ParticleTypes.SOUL_FIRE_FLAME, 0.1F),
	SUBZERO("subzero", ParticleTypes.END_ROD, 0.3F);

	public static final Temperature[] VALUES = values();
	public static final Map<String, Temperature> MAP = Arrays.stream(VALUES).collect(Collectors.toMap(t -> t.id, t -> t));

	private final String id;
	private final Component name;
	private final ResourceLocation texture;
	private final Icon icon;
	public final ParticleOptions particleOptions;
	public final float particleOffset;

	Temperature(String id, ParticleOptions particleOptions, float particleOffset) {
		this.id = id;
		this.particleOptions = particleOptions;
		this.particleOffset = particleOffset;

		name = Component.translatable("ftbjarmod.temperature." + this.id);
		texture = FTBOBB.id("textures/gui/temperature/" + this.id + ".png");
		icon = Icon.getIcon(texture);
	}

	@Override
	public String getSerializedName() {
		return id;
	}

	public Component getName() {
		return name;
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	public Icon getIcon() {
		return icon;
	}

	public static Temperature byName(String name) {
		return MAP.getOrDefault(name.toLowerCase(), NONE);
	}

}