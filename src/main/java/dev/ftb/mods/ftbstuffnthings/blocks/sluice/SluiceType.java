package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import org.apache.commons.lang3.text.WordUtils;

public enum SluiceType implements StringRepresentable {
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    MANGROVE("mangrove"),
    CHERRY("cherry", SoundType.CHERRY_WOOD, true),
    PALE_OAK("pale_oak"),
    CRIMSON("crimson", SoundType.NETHER_WOOD, true),
    WARPED("warped", SoundType.NETHER_WOOD, true),
    BAMBOO("bamboo", SoundType.BAMBOO_WOOD, true),
    IRON("iron", 0.8, 0.6, 12000,
            true, false, false, 0, SoundType.METAL, false),
    DIAMOND("diamond", 0.6, 0.75, 12000,
            true, true, false, 0, SoundType.METAL, false),
    NETHERITE("netherite", 0.4, 0.5, 12000,
            true, true, true, 40, SoundType.NETHERITE_BLOCK, false);

    private final String name;
    public final double defTimeMod;
    public final double defFluidMod;
    public final int defCapacity;
    public final boolean defItemIO;
    public final boolean defFluidIO;
    public final boolean defUpgradeable;
    public final int defEnergyUsage;
    public final SoundType soundType;
    public final boolean isWood;

    SluiceType(String name) {
        this(name, SoundType.WOOD, true);
    }

    SluiceType(String name, SoundType soundType, boolean isWood) {
        this(name, 1.0, 1.0, 12000, false, false, false, 0, soundType, isWood);
    }

    SluiceType(String name, double defTimeMod, double defFluidMod, int defCapacity, boolean defItemIO, boolean defFluidIO, boolean defUpgradeable, int defEnergyUsage, SoundType soundType, boolean isWood) {
        this.name = name;
        this.defTimeMod = defTimeMod;
        this.defFluidMod = defFluidMod;
        this.defCapacity = defCapacity;
        this.defItemIO = defItemIO;
        this.defFluidIO = defFluidIO;
        this.defUpgradeable = defUpgradeable;
        this.defEnergyUsage = defEnergyUsage;
        this.soundType = soundType;
        this.isWood = isWood;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String description() {
        return WordUtils.capitalizeFully(name.replace('_', ' ')) + " Sluice";
    }
}
