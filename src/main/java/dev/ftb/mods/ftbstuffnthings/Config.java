package dev.ftb.mods.ftbstuffnthings;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceType;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.CONFIG_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;

public class Config {
    private static final SNBTConfig CONFIG = SNBTConfig.create(FTBStuffNThings.MODID);

    private static final SNBTConfig GENERAL_CONFIG = CONFIG.addGroup("general");
    public static final BooleanValue INCLUDE_DEV_RECIPES = GENERAL_CONFIG.addBoolean("include_dev_recipes", false)
            .comment("If true, dev/testing recipes will be available outside a development environment", "Leave this false unless actually testing the mod.");

    private static final SNBTConfig SLUICE_CONFIG = CONFIG.addGroup("sluice");
    private static final Map<SluiceType,SluiceProperties> SLUICE_PROPERTIES = Util.make(new EnumMap<>(SluiceType.class), map -> {
        for (SluiceType type : SluiceType.values()) {
            SNBTConfig config = SLUICE_CONFIG.addGroup(type.getSerializedName());
            map.put(type, new SluiceProperties(
                    config.addDouble("timeMultiplier", type.defTimeMod)
                            .comment("How long it takes to process a resource in this Sluice (multiplier for recipe base tick time)"),
                    config.addDouble("fluidMultiplier", type.defFluidMod)
                            .comment("How much fluid is used per recipe (multiplier for recipe's fluid consumption rate)"),
                    config.addInt("tankCapacity", type.defCapacity)
                            .comment("How much fluid this sluice's tank can carry (in mB)"),
                    config.addBoolean("allowsIO", type.defItemIO)
                            .comment("Can items be piped in? False = items only clicked in manually"),
                    config.addBoolean("allowsTank", type.defFluidIO)
                            .comment("Can fluid be piped in? False = need to use adjacent Pump"),
                    config.addBoolean("upgradeable", type.defUpgradeable)
                            .comment("Can sluice be upgraded?"),
                    config.addInt("energyUsage", type.defEnergyUsage)
                            .comment("FE cost per use")
            ));
        }
    });

    private static final SNBTConfig AUTOHAMMER_CONFIG = CONFIG.addGroup("autohammer");
    public static final IntValue IRON_HAMMER_SPEED = AUTOHAMMER_CONFIG.addInt("stone_hammer_speed", 50, 1, 100000)
            .comment("Speed of the iron auto-hammer as ticks taken to process a block");
    public static final IntValue GOLD_HAMMER_SPEED = AUTOHAMMER_CONFIG.addInt("gold_hammer_speed", 40, 1, 100000)
            .comment("Speed of the gold auto-hammer as ticks taken to process a block");
    public static final IntValue DIAMOND_HAMMER_SPEED = AUTOHAMMER_CONFIG.addInt("diamond_hammer_speed", 30, 1, 100000)
            .comment("Speed of the diamond auto-hammer as ticks taken to process a block");
    public static final IntValue NETHERITE_HAMMER_SPEED = AUTOHAMMER_CONFIG.addInt("netherite_hammer_speed", 15, 1, 100000)
            .comment("Speed of the netherite auto-hammer as ticks taken to process a block");

    private static final SNBTConfig COBBLEGEN_CONFIG = CONFIG.addGroup("cobblegen");

    public static final IntValue DELAY_PER_OPERATION = COBBLEGEN_CONFIG.addInt("cobblegen_tick_rate", 20, 1, Integer.MAX_VALUE)
            .comment("The delay between each cobble generation in ticks");

    public static final IntValue STONE_COBBLEGEN_AMOUNT = COBBLEGEN_CONFIG.addInt("stone_cobblegen_amount", 1, 1, 1000)
            .comment("Amount of cobble the stone cobblegen produces per tick");
    public static final IntValue IRON_COBBLEGEN_AMOUNT = COBBLEGEN_CONFIG.addInt("iron_cobblegen_amount", 8, 1, 1000)
            .comment("Amount of cobble the iron cobblegen produces per tick");
    public static final IntValue GOLD_COBBLEGEN_AMOUNT = COBBLEGEN_CONFIG.addInt("gold_cobblegen_amount", 16, 1, 1000)
            .comment("Amount of cobble the gold cobblegen produces per tick");
    public static final IntValue DIAMOND_COBBLEGEN_AMOUNT = COBBLEGEN_CONFIG.addInt("diamond_cobblegen_amount", 32, 1, 1000)
            .comment("Amount of cobble the diamond cobblegen produces per tick");
    public static final IntValue NETHERITE_COBBLEGEN_AMOUNT = COBBLEGEN_CONFIG.addInt("netherite_cobblegen_amount", 64, 1, 1000)
            .comment("Amount of cobble the netherite cobblegen produces per tick");

    private static final SNBTConfig BASALTGEN_CONFIG = CONFIG.addGroup("basaltgen");

    public static final IntValue STONE_BASALTGEN_SPEED = BASALTGEN_CONFIG.addInt("stone_basaltgen_speed", 1, 1, 1000)
            .comment("Amount of basalt the stone basalt generator produces per tick");
    public static final IntValue IRON_BASALTGEN_SPEED = BASALTGEN_CONFIG.addInt("iron_basaltgen_speed", 8, 1, 1000)
            .comment("Amount of basalt the iron basalt generator produces per tick");
    public static final IntValue GOLD_BASALTGEN_SPEED = BASALTGEN_CONFIG.addInt("gold_basaltgen_speed", 16, 1, 1000)
            .comment("Amount of basalt the gold basalt generator produces per tick");
    public static final IntValue DIAMOND_BASALTGEN_SPEED = BASALTGEN_CONFIG.addInt("diamond_basaltgen_speed", 32, 1, 1000)
            .comment("Amount of basalt the diamond basalt generator produces per tick");
    public static final IntValue NETHERITE_BASALTGEN_SPEED = BASALTGEN_CONFIG.addInt("netherite_basaltgen_speed", 64, 1, 1000)
            .comment("Amount of basalt the netherite basalt generator produces per tick");


    private static final SNBTConfig WATER_STRAINER_CONFIG = CONFIG.addGroup("water_strainer");
    public static final IntValue STRAINER_TICK_RATE = WATER_STRAINER_CONFIG.addInt("strainer_tick_rate", 20, 1, Integer.MAX_VALUE)
            .comment("The delay between each strainer generation in ticks");
    public static final StringValue STRAINER_LOOT_TABLE = WATER_STRAINER_CONFIG.addString("strainer_loot_table", FTBStuffNThings.MODID + ":custom/water_strainer_test")
            .comment("Location of the loot table used to generate strainer loot from");

    private static final SNBTConfig PUMP_CONFIG = CONFIG.addGroup("pump");
    public static final IntValue PUMP_MAX_CHARGE = PUMP_CONFIG.addInt("pump_max_charge", 6000, 1000, Integer.MAX_VALUE)
            .comment("Maximum charge the Pump can have. 20 charge is consumed for every 1000mB of water pumped");
    public static final IntValue PUMP_CHARGEUP_AMOUNT = PUMP_CONFIG.addInt("pump_chargeup_amount", 14, 1, Integer.MAX_VALUE)
            .comment("Amount of charge to add each time the Pump is right-clicked by a player. 20 charge is consumed for every 1000mB of water pumped");
    public static final IntValue PUMP_FLUID_TRANSFER = PUMP_CONFIG.addInt("pump_fluid_transfer", 1000, 1, Integer.MAX_VALUE)
            .comment("Amount of water to transfer to each valid neighbouring fluid tank on each work cycle (every 20 ticks).");


    public static void init() {
        loadDefaulted(CONFIG, CONFIG_DIR, FTBStuffNThings.MODID, FTBStuffNThings.MODID + ".snbt");
    }

    public static Lazy<SluiceProperties> makeSluiceProperties(SluiceType type) {
        return Lazy.of(() -> SLUICE_PROPERTIES.get(type));
    }

    public static Optional<ResourceLocation> getStrainerLootTable() {
        return Optional.ofNullable(ResourceLocation.tryParse(STRAINER_LOOT_TABLE.get()));
    }
}
