package dev.ftb.mods.ftbstuffnthings.util;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.function.UnaryOperator;

public class TextUtil {
    public static final UnaryOperator<Style> COLOUR_HIGHLIGHT
            = (style) -> style.withColor(TextColor.fromRgb(0xfcb95b));
    public static final UnaryOperator<Style> COLOUR_TRUE
            = (style) -> style.withColor(TextColor.fromRgb(0x4ecc8d));
    public static final UnaryOperator<Style> COLOUR_FALSE
            = (style) -> style.withColor(TextColor.fromRgb(0xfd6d5d)).withStrikethrough(true);

    public static UnaryOperator<Style> ofBoolean(boolean b) {
        return b ? COLOUR_TRUE : COLOUR_FALSE;
    }
}
