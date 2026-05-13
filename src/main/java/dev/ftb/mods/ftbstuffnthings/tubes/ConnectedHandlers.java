package dev.ftb.mods.ftbstuffnthings.tubes;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.JarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public record ConnectedHandlers(List<BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>> itemHandlers,
                                List<BlockCapabilityCache<ResourceHandler<FluidResource>, Direction>> fluidHandlers) {
    public static ConnectedHandlers create() {
        return new ConnectedHandlers(new ArrayList<>(), new ArrayList<>());
    }

    public void checkAndAddHandlers(ServerLevel level, BlockPos pos, Direction dir) {
        if (level.getCapability(Capabilities.Item.BLOCK, pos, dir) != null) {
            itemHandlers.add(BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, dir));
        }
        if (level.getCapability(Capabilities.Fluid.BLOCK, pos, dir) != null) {
            fluidHandlers.add(BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, level, pos, dir));
        }
    }

    public ExtractionContext findIngredients(JarRecipe recipe) {
        ExtractionContext context = new ExtractionContext();

        for (var input : recipe.allInputs()) {
            input.ifLeft(fluid -> findFluid(fluid, context))
                    .ifRight(item -> findItem(item, context));
            if (context.isInsufficient()) {
                break;
            }
        }

        return context;
    }

    public boolean distributeOutputs(BlockEntity jar, JarRecipe recipe) {
        List<ItemStack> excessItems = new ArrayList<>();

        for (ItemStackTemplate template : recipe.getOutputItems()) {
            int remaining = template.count();
            for (var handler : itemHandlers) {
                if (handler.getCapability() != null) {
                    ItemStack excess = ItemUtil.insertItemReturnRemaining(handler.getCapability(), template.create(), false, null);
                    remaining -= template.count() - excess.getCount();
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
            if (remaining > 0) {
                excessItems.add(template.create());
            }
        }

        return false;
    }

    // TODO needs to be redone if we ever add tube networks back...

    private void findFluid(SizedFluidIngredient ingredient, ExtractionContext context) {
//        int remaining = ingredient.amount();
//
//        for (var fluidCaches : fluidHandlers) {
//            var handler = fluidCaches.getCapability();
//            if (handler != null) {
//                FluidStack stack = handler.drain(remaining, IFluidHandler.FluidAction.SIMULATE);
//                if (ingredient.ingredient().test(stack)) {
//                    remaining -= stack.getAmount();
//                    context.addFluidSource(handler, stack);
//                    if (remaining <= 0) {
//                        break;
//                    }
//                }
//            }
//        }
//        if (remaining > 0) {
//            context.markInsufficient();
//        }
    }

    private void findItem(SizedIngredient ingredient, ExtractionContext context) {
//        int remaining = ingredient.count();
//
//        for (var itemCaches : itemHandlers) {
//            IItemHandler handler = itemCaches.getCapability();
//            if (handler != null) {
//                for (int i = 0; i < handler.getSlots(); i++) {
//                    ItemStack stack = handler.extractItem(i, remaining, true);
//                    if (ingredient.ingredient().test(stack)) {
//                        remaining -= stack.getCount();
//                        context.addItemSource(handler, stack, i);
//                        if (remaining <= 0) {
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        if (remaining > 0) {
//            context.markInsufficient();
//        }
    }

    public static class ExtractionContext {
        private boolean insufficient;
        private final List<FluidSource> fluidSources = new ArrayList<>();
        private final List<ItemSource> itemSources = new ArrayList<>();

        public ExtractionContext() {
            this.insufficient = false;
        }

        public boolean isInsufficient() {
            return insufficient;
        }

        public void markInsufficient() {
            insufficient = true;
        }

        public void addFluidSource(ResourceHandler<FluidResource> handler, FluidStack fluidStack) {
            fluidSources.add(new FluidSource(handler, fluidStack));
        }

        public void addItemSource(ResourceHandler<ItemResource> handler, ItemStack itemStack, int slot) {
            itemSources.add(new ItemSource(handler, itemStack, slot));
        }

        public boolean apply() {
            // actually extract the resource from the handlers - should always succeed!
            // - as long as this context is used on the same tick that it was created

            return true;
        }
    }

    private record FluidSource(ResourceHandler<FluidResource> handler, FluidStack fluidStack) {
    }

    private record ItemSource(ResourceHandler<ItemResource> handler, ItemStack itemStack, int slot) {
    }
}
