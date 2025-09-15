package dev.ftb.mods.ftbstuffnthings.blocks.woodbasin;

import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.WoodenBasinRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class WoodenBasinBlockEntity extends BlockEntity {
    private final FluidTank tank;
    private FluidStack prevFluid = FluidStack.EMPTY;

    public WoodenBasinBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntitiesRegistry.WOODEN_BASIN.get(), blockPos, blockState);

        tank = new FluidTank(4000) {
            @Override
            protected void onContentsChanged() {
                fluidChanged();
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("Tank", tank.writeToNBT(provider, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        tank.readFromNBT(provider, tag.getCompound("Tank"));
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

    public void trySqueezing(Entity fallingEntity) {
         RecipeCaches.WOODEN_BASIN.getCachedRecipe(this::searchForRecipe, this::genRecipeHash).ifPresent(h -> {
            var recipe = h.value();

            if (recipe.getProductionChance() >= 1f || level.getRandom().nextFloat() < recipe.getProductionChance()) {
                int filled = tank.fill(recipe.getFluid(), IFluidHandler.FluidAction.SIMULATE);
                if (filled == recipe.getFluid().getAmount()) {
                    tank.fill(recipe.getFluid(), IFluidHandler.FluidAction.EXECUTE);
                    if (recipe.getBlockConsumeChance() >= 1f || level.getRandom().nextFloat() < recipe.getBlockConsumeChance()) {
                        level.destroyBlock(getBlockPos().above(), recipe.dropItems(), fallingEntity);
                    } else {
                        level.playSound(null, getBlockPos().above(), SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1f, 1f);
                        sendParticles(fallingEntity, recipe.getFluid().getFluid());
                    }
                } else {
                    if (fallingEntity instanceof Player p) {
                        p.displayClientMessage(Component.translatable("ftbstuff.wooden_basin.full_tank").withStyle(ChatFormatting.GOLD), true);
                    }
                }
            }
        });
    }

    private void sendParticles(Entity entity, Fluid fluid) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos()), false).forEach(player -> {
                ParticleOptions particle = fluid.getFluidType().getDripInfo() != null ?
                        fluid.getFluidType().getDripInfo().dripParticle() :
                        ParticleTypes.DRIPPING_DRIPSTONE_WATER;
                if (particle == null) particle = ParticleTypes.DRIPPING_DRIPSTONE_WATER;
                Vec3 pos = Vec3.atCenterOf(getBlockPos()).add(0, 1.8, 0);
                player.connection.send(new ClientboundLevelParticlesPacket(particle, true, pos.x, pos.y - 0.5, pos.z, 0.3f, 0.1f, 0.3f, 0.05f, 20));
            });
        }
    }

    private int genRecipeHash() {
        BlockState blockAbove = getLevel().getBlockState(getBlockPos().above());

        return Objects.hash(blockAbove);
    }

    private Optional<RecipeHolder<WoodenBasinRecipe>> searchForRecipe() {
        return getLevel().getRecipeManager().getRecipesFor(RecipesRegistry.WOODEN_BASIN_TYPE.get(), NoInventory.INSTANCE, getLevel()).stream()
                .filter(r -> r.value().testInput(new BlockInWorld(level, getBlockPos().above(), true)))
                .findFirst();
    }

    private void fluidChanged() {
        setChanged();

        if (!level.isClientSide() && fluidsDifferentEnough(prevFluid)) {
            // sync contained fluid to client
            prevFluid = tank.getFluid().copy();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
    }

    private boolean fluidsDifferentEnough(FluidStack prev) {
        if (prev.getFluid() != tank.getFluid().getFluid()) {
            return true;
        }
        int a1 = prev.getAmount() / (tank.getCapacity() / 10);
        int a2 = tank.getFluid().getAmount() / (tank.getCapacity() / 10);
        return a1 != a2;
    }

    public IFluidHandler getFluidHandler() {
        return tank;
    }

    public FluidTank getTank() {
        return tank;
    }
}
