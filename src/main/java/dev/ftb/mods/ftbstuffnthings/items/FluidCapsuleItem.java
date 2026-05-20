package dev.ftb.mods.ftbstuffnthings.items;

import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Consumer;

public class FluidCapsuleItem extends Item {
    public FluidCapsuleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack of(FluidStack fluidStack) {
        ItemStack stack = new ItemStack(ItemsRegistry.FLUID_CAPSULE.get());
        stack.set(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.copyOf(fluidStack));
        return stack;
    }

    public static FluidStack getFluid(ItemStack stack) {
        return stack.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

        SimpleFluidContent content = itemStack.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY);
        if (!content.isEmpty()) {
            builder.accept(MiscUtil.makeFluidStackDesc(content.copy()));
        }
    }

    public static class FluidHandler extends ItemAccessFluidHandler {
        public FluidHandler(ItemAccess container) {
            super(container, ComponentsRegistry.STORED_FLUID.get(), FluidType.BUCKET_VOLUME);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            // only allow filling if it's completely empty
            return getAmountAsInt(index) == 0 ? super.insert(index, resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            int amountInCapsule = getAmountAsInt(index);
            int extracted = super.extract(index, resource, amount, transaction);
            if (extracted == amountInCapsule) {
                // extracted all of the fluid; shrink the container stack
                itemAccess.extract(itemAccess.getResource(), 1, transaction);
            }
            return extracted;
        }
    }
}
