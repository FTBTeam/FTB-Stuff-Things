package dev.ftb.mods.ftbstuffnthings.client;

import dev.ftb.mods.ftblibrary.util.neoforge.FluidKey;
import dev.ftb.mods.ftbstuffnthings.items.FluidCapsuleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FluidCapsuleColorHandler {
    private static final Map<FluidKey, Integer> COLOR_MAP = new HashMap<>();

    public static int getColor(ItemStack capsuleStack) {
        FluidStack fStack = FluidCapsuleItem.getFluid(capsuleStack);
        return COLOR_MAP.computeIfAbsent(new FluidKey(fStack), k -> calculateFluidColor(fStack));
    }

    private static int calculateFluidColor(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return 0xFF000000;
        }

        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        Identifier fluidStill = Objects.requireNonNullElse(renderProps.getStillTexture(fluidStack), MissingTextureAtlasSprite.getLocation());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);

        float[] tint = GuiUtil.decomposeColorF(renderProps.getTintColor(fluidStack));  // ARGB
        float[] rgba = {0F, 0F, 0F, 0F};

        for (int y = 0; y < sprite.contents().height(); y++) {
            for (int x = 0; x < sprite.contents().width(); x++) {
                int color = sprite.getPixelRGBA(0, x, y);
                float a = (color >> 24 & 0xFF);
                if (a > 0F) {
                    rgba[0] += (color >> 0  & 0xFF) * tint[1];
                    rgba[1] += (color >> 8  & 0xFF) * tint[2];
                    rgba[2] += (color >> 16 & 0xFF) * tint[3];
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
}
