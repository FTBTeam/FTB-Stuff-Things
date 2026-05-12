package dev.ftb.mods.ftbstuffnthings.util;

import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class MiscUtil {
    public static NonNullList<ItemStack> getItemsInHandler(ResourceHandler<ItemResource> handler) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < handler.size(); i++) {
            ItemStack stack = ItemUtil.getStack(handler, i);
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        return NonNullList.copyOf(items);
    }

    public static Component makeFluidStackDesc(FluidStack stack) {
        return Component.translatable("ftbstuff.tooltip.fluid", stack.getAmount(), stack.getHoverName()).withStyle(ChatFormatting.AQUA);
    }

    public static DataResult<Double> validateChanceRange(double d) {
        return d > 0.0 && d <= 1.0 ? DataResult.success(d) : DataResult.error(() -> "must be in range (0.0 -> 1.0]");
    }

    public static List<ItemStack> getItemsForSizedIngredient(SizedIngredient ingredient) {
        return ingredient.ingredient().items()
                .map(holder -> new ItemStack(holder.value(), ingredient.count())).toList();
    }

    public static List<FluidStack> getFluidsForSizedIngredient(SizedFluidIngredient ingredient) {
        return ingredient.ingredient().fluids().stream()
                .map(holder -> new FluidStack(holder.value(), ingredient.amount())).toList();
    }
}
