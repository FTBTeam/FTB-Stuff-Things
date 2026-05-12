package dev.ftb.mods.ftbstuffnthings.blocks.woodbasin;

import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.WoodenBasinRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class WoodenBasinBlockEntity extends BlockEntity {
    private final FluidStacksResourceHandler tank;

    public WoodenBasinBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntitiesRegistry.WOODEN_BASIN.get(), blockPos, blockState);

        tank = new FluidStacksResourceHandler(1, 4000) {
            @Override
            protected void onContentsChanged(int index, FluidStack previousContents) {
                fluidChanged(previousContents);
            }
        };
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
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(output);
        return output.buildResult();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void trySqueezing(ServerLevel serverLevel, Entity fallingEntity) {
         RecipeCaches.WOODEN_BASIN.getCachedRecipe(serverLevel, this::searchForRecipe, this::genRecipeHash).ifPresent(h -> {
            var recipe = h.value();

            if (recipe.getProductionChance() >= 1f || serverLevel.getRandom().nextFloat() < recipe.getProductionChance()) {
                try (Transaction tx = Transaction.openRoot()) {
                    FluidStackTemplate result = recipe.getFluidResult();
                    int filled = tank.insert(FluidResource.of(result.fluid()), result.amount(), tx);

                    if (filled == result.amount()) {
                        tx.commit();
                        if (recipe.getBlockConsumeChance() >= 1f || serverLevel.getRandom().nextFloat() < recipe.getBlockConsumeChance()) {
                            serverLevel.destroyBlock(getBlockPos().above(), recipe.dropItems(), fallingEntity);
                        } else {
                            serverLevel.playSound(null, getBlockPos().above(), SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1f, 1f);
                            sendParticles(result.fluid().value());
                        }
                    } else {
                        if (fallingEntity instanceof Player p) {
                            p.sendOverlayMessage(Component.translatable("ftbstuff.wooden_basin.full_tank").withStyle(ChatFormatting.GOLD));
                        }
                    }
                }
            }
        });
    }

    private void sendParticles(Fluid fluid) {
        if (level instanceof ServerLevel serverLevel) {
            ParticleOptions particle = fluid.getFluidType().getDripInfo() != null ?
                    fluid.getFluidType().getDripInfo().dripParticle() :
                    ParticleTypes.DRIPPING_DRIPSTONE_WATER;
            if (particle == null) {
                particle = ParticleTypes.DRIPPING_DRIPSTONE_WATER;
            }
            Vec3 pos = Vec3.atCenterOf(getBlockPos()).add(0.0, 1.3, 0.0);
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(getBlockPos()), false)) {
                player.connection.send(new ClientboundLevelParticlesPacket(particle, false, true,
                        pos.x, pos.y, pos.z, 0.3f, 0.1f, 0.3f, 0.05f, 20)
                );
            }
        }
    }

    private int genRecipeHash() {
        BlockState blockAbove = getLevel().getBlockState(getBlockPos().above());

        return Objects.hash(blockAbove);
    }

    private Optional<RecipeHolder<WoodenBasinRecipe>> searchForRecipe(ServerLevel serverLevel) {
        return serverLevel.getServer().getRecipeManager().recipeMap().getRecipesFor(RecipesRegistry.WOODEN_BASIN_TYPE.get(), NoInventory.INSTANCE, serverLevel)
                .filter(r -> r.value().testInput(new BlockInWorld(serverLevel, getBlockPos().above(), true)))
                .findFirst();
    }

    private void fluidChanged(FluidStack previousContents) {
        setChanged();

        if (!level.isClientSide() && fluidsDifferentEnough(previousContents)) {
            // sync contained fluid to client
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
    }

    private boolean fluidsDifferentEnough(FluidStack prev) {
        if (prev.getFluid() != tank.getResource(0).getFluid()) {
            return true;
        }
        int capacity = tank.getCapacityAsInt(0, FluidResource.EMPTY);
        int a1 = prev.getAmount() / (capacity / 10);
        int a2 = tank.getAmountAsInt(0) / (capacity / 10);
        return a1 != a2;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return tank;
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }
}
