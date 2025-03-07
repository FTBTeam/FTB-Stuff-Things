package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public enum SluiceType implements StringRepresentable {
    OAK("oak", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Oak::new),
    SPRUCE("spruce", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Spruce::new),
    BIRCH("birch", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Birch::new),
    JUNGLE("jungle", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Jungle::new),
    ACACIA("acacia", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Acacia::new),
    DARK_OAK("dark_oak", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.DarkOak::new),
    MANGROVE("mangrove", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Mangrove::new),
    CHERRY("cherry", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Cherry::new),
    PALE_OAK("pale_oak", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.PaleOak::new),
    CRIMSON("crimson", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Crimson::new),
    WARPED("warped", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Warped::new),
    BAMBOO("bamboo", 1.0, 1.0, 12000,
            false, false, false, 0,
            SluiceBlockEntity.Bamboo::new),
    IRON("iron", 0.8, 0.6, 12000,
            true, false, false, 0,
            SluiceBlockEntity.Iron::new),
    DIAMOND("diamond", 0.6, 0.75, 12000,
            true, true, false, 0,
            SluiceBlockEntity.Diamond::new),
    NETHERITE("netherite", 0.4, 0.5, 12000,
            true, true, true, 40,
            SluiceBlockEntity.Netherite::new);

    private final String name;
    public final double defTimeMod;
    public final double defFluidMod;
    public final int defCapacity;
    public final boolean defItemIO;
    public final boolean defFluidIO;
    public final boolean defUpgradeable;
    public final int defEnergyUsage;
    private final BiFunction<BlockPos, BlockState, ? extends SluiceBlockEntity> beFactory;

    SluiceType(String name, double defTimeMod, double defFluidMod, int defCapacity, boolean defItemIO, boolean defFluidIO, boolean defUpgradeable, int defEnergyUsage, BiFunction<BlockPos, BlockState, ? extends SluiceBlockEntity> beFactory) {
        this.name = name;
        this.defTimeMod = defTimeMod;
        this.defFluidMod = defFluidMod;
        this.defCapacity = defCapacity;
        this.defItemIO = defItemIO;
        this.defFluidIO = defFluidIO;
        this.defUpgradeable = defUpgradeable;
        this.defEnergyUsage = defEnergyUsage;
        this.beFactory = beFactory;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public SluiceBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return beFactory.apply(pos, state);
    }
}
