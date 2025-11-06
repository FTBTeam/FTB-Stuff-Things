package dev.ftb.mods.ftbstuffnthings.blocks.dripper;

import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.DripperRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class DripperBlockEntity extends BlockEntity {
	private final FluidTank tank;
	private Fluid prevFluid = null;

    public DripperBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntitiesRegistry.DRIPPER.get(), pos, state);

		tank = new FluidTank(4000) {
			@Override
			protected void onContentsChanged() {
				fluidChanged();
			}
		};
	}

	public FluidTank getTank() {
		return tank;
	}

	public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
		tag.put("Tank", tank.writeToNBT(provider, new CompoundTag()));
	}

	public void readData(CompoundTag tag, HolderLookup.Provider provider) {
		tank.readFromNBT(provider, tag.getCompound("Tank"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		writeData(tag, provider);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);

		readData(tag, provider);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		// server-side, chunk loading
		return Util.make(new CompoundTag(), tag -> saveAdditional(tag, provider));
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private void fluidChanged() {
		setChanged();

		if (!level.isClientSide() && prevFluid != tank.getFluid().getFluid()) {
			prevFluid = tank.getFluid().getFluid();
			// sync contained fluid to client, so it knows what sort of drip particle to play
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
		}
	}

	public void serverTick(ServerLevel serverLevel) {
        if (serverLevel.getGameTime() % 20 == 0) {
			FluidState state = serverLevel.getFluidState(getBlockPos().above());
			if (state.is(Tags.Fluids.WATER) && state.isSource()) {
				tank.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
			}
            if (!tank.isEmpty()) {
                RecipeHolder<DripperRecipe> currentRecipe = RecipeCaches.DRIPPER.getCachedRecipe(this::searchForRecipe, this::genRecipeHash).orElse(null);

                level.setBlock(worldPosition, getBlockState().setValue(DripperBlock.ACTIVE, !tank.getFluid().isEmpty() && currentRecipe != null), Block.UPDATE_ALL);

                if (currentRecipe != null) {
                    DripperRecipe recipe = currentRecipe.value();
                    boolean success = false;
                    if (tank.getFluidAmount() >= recipe.getFluid().getAmount()) {
                        if (serverLevel.random.nextDouble() < recipe.getChance()) {
                            level.setBlock(getBlockPos().below(), recipe.getOutputState(), Block.UPDATE_ALL);
                            success = true;
                        }
                        if (success || recipe.consumeFluidOnFail()) {
                            tank.drain(recipe.getFluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
	}

	private int genRecipeHash() {
		int fluidHash = FluidStack.hashFluidAndComponents(tank.getFluid());
		BlockState blockBelow = getLevel().getBlockState(getBlockPos().below());

		return Objects.hash(fluidHash, blockBelow);
	}

	private Optional<RecipeHolder<DripperRecipe>> searchForRecipe() {
		return level.getRecipeManager().getRecipesFor(RecipesRegistry.DRIP_TYPE.get(), NoInventory.INSTANCE, level).stream()
				.filter(r -> r.value().testInput(tank.getFluid(), getLevel(), getBlockPos().below()))
				.findFirst();
	}
}