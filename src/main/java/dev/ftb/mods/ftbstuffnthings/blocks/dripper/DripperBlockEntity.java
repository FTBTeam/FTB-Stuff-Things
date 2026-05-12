package dev.ftb.mods.ftbstuffnthings.blocks.dripper;

import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.DripperRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class DripperBlockEntity extends BlockEntity {
	private final FluidStacksResourceHandler tank;

    public DripperBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntitiesRegistry.DRIPPER.get(), pos, state);

		tank = new FluidStacksResourceHandler(1, 4000) {
			@Override
			protected void onContentsChanged(int index, FluidStack previousContents) {
				fluidChanged(previousContents);
			}
		};
	}

	public FluidStacksResourceHandler getTank() {
		return tank;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		output.putChild("Tank", tank);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		tank.deserialize(input.childOrEmpty("Tank"));
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		// server-side, chunk loading
		return Util.make(new CompoundTag(), tag -> saveAdditional(TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider)));
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private void fluidChanged(FluidStack previousContents) {
		setChanged();

		if (!level.isClientSide() && previousContents.getFluid() != tank.getResource(0).getFluid()) {
			// sync contained fluid to client, so it knows what sort of drip particle to play
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
		}
	}

	public void serverTick(ServerLevel serverLevel) {
        if (serverLevel.getGameTime() % 20 == 0 && getBlockState().hasProperty(DripperBlock.ACTIVE)) {
			FluidState state = serverLevel.getFluidState(getBlockPos().above());
			if (state.is(Tags.Fluids.WATER) && state.isSource()) {
				try (Transaction tx = Transaction.openRoot()) {
					tank.insert(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
					tx.commit();
				}
			}
			boolean active = getBlockState().getValue(DripperBlock.ACTIVE);
			boolean newActive = false;
            if (!tank.getResource(0).isEmpty()) {
                var currentRecipe = RecipeCaches.DRIPPER.getCachedRecipe(serverLevel, this::searchForRecipe, this::genRecipeHash);
                if (currentRecipe.isPresent()) {
                    DripperRecipe recipe = currentRecipe.get().value();
                    boolean success = false;
                    if (tank.getAmountAsInt(0) >= recipe.getFluid().getAmount()) {
						newActive = true;
						if (serverLevel.getRandom().nextDouble() < recipe.getChance()) {
							serverLevel.setBlock(getBlockPos().below(), recipe.getOutputState(), Block.UPDATE_ALL);
							success = true;
						}
						if (success || recipe.consumeFluidOnFail()) {
							try (Transaction tx = Transaction.openRoot()) {
								tank.extract(FluidResource.of(recipe.getFluid()), recipe.getFluid().amount(), tx);
								tx.commit();
							}
                        }
                    }
                }
            }
			if (active != newActive) {
				serverLevel.setBlock(worldPosition, getBlockState().setValue(DripperBlock.ACTIVE, newActive), Block.UPDATE_ALL);
			}
        }
	}

	private int genRecipeHash() {
		int fluidHash = FluidStack.hashFluidAndComponents(FluidUtil.getStack(tank, 0));
		BlockState blockBelow = getLevel().getBlockState(getBlockPos().below());

		return Objects.hash(fluidHash, blockBelow);
	}

	private Optional<RecipeHolder<DripperRecipe>> searchForRecipe(ServerLevel serverLevel) {
		return serverLevel.getServer().getRecipeManager().recipeMap().getRecipesFor(RecipesRegistry.DRIP_TYPE.get(), NoInventory.INSTANCE, serverLevel)
				.filter(r -> r.value().testInput(tank.getResource(0).toStack(tank.getAmountAsInt(0)), serverLevel, getBlockPos().below()))
				.findFirst();
	}
}