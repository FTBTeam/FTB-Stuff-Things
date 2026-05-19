package dev.ftb.mods.ftbstuffnthings.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.items.FluidCapsuleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record FluidCapsuleTintSource(int defColor) implements ItemTintSource {
    public static final int DEFAULT_COLOR = 0xFF000000;
    public static final FluidCapsuleTintSource DEFAULT = new FluidCapsuleTintSource(DEFAULT_COLOR);

    public static final MapCodec<FluidCapsuleTintSource> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.optionalFieldOf("default_color", DEFAULT_COLOR).forGetter(FluidCapsuleTintSource::defColor)
    ).apply(builder, FluidCapsuleTintSource::new));

    private static final Map<Fluid, Integer> COLOR_MAP = new HashMap<>();

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        FluidStack fStack = FluidCapsuleItem.getFluid(itemStack);

        return COLOR_MAP.computeIfAbsent(fStack.getFluid(), _ -> calculateFluidColor(fStack));
    }

    private static int calculateFluidColor(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return DEFAULT_COLOR;
        }

        Fluid fluid = fluidStack.getFluid();
        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        int tintColor = DEFAULT_COLOR | (model.fluidTintSource() == null ?
                0xFFFFFFF :
                model.fluidTintSource().color(fluid.defaultFluidState()));

        float[] tint = GuiUtil.decomposeColorF(tintColor);  // ARGB
        float[] rgba = {0F, 0F, 0F, 0F};

        for (int y = 0; y < sprite.contents().height(); y++) {
            for (int x = 0; x < sprite.contents().width(); x++) {
                int color = sprite.getPixelRGBA(0, x, y);
                float a = (color >> 24 & 0xFF);
                if (a > 0F) {
                    rgba[0] += (color >> 16 & 0xFF) * tint[3];
                    rgba[1] += (color >> 8  & 0xFF) * tint[2];
                    rgba[2] += (color >> 0  & 0xFF) * tint[1];
                    rgba[3] += a;
                }
            }
        }
        int nPixels = sprite.contents().width() * sprite.contents().height();
        rgba[0] /= nPixels;
        rgba[1] /= nPixels;
        rgba[2] /= nPixels;
        rgba[3] /= nPixels;

        return (int) (rgba[3]) << 24 | (int) (rgba[0]) << 16 | (int) (rgba[1]) << 8 | (int) (rgba[2]);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
