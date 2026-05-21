package dev.ftb.mods.ftbstuffnthings.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Consumer;

public class ClientUtil {
    public static <T> Optional<T> getBlockEntityAt(BlockPos pos, Class<T> cls) {
        Level level = Minecraft.getInstance().level;
        if (level != null && level.isLoaded(pos)) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te != null && cls.isAssignableFrom(te.getClass())) {
                //noinspection unchecked
                return Optional.of((T) te);
            }
        }
        return Optional.empty();
    }

    public static void maybeAddBlockTooltip(ItemStack stack, Consumer<Component> tooltips) {
        String tooltipKey = stack.getItem().getDescriptionId() + ".tooltip";
        if (I18n.exists(tooltipKey)) {
            tooltips.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
