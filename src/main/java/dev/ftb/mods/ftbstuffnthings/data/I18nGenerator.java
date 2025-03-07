package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I18nGenerator extends LanguageProvider {
    private static final String[] COMPRESSED_PREFIXES = new String[] { "", "Double ", "Triple " };
    private static final Map<String, String> COMPRESSED_BLOCKS = Util.make(new HashMap<>(), map -> {
        map.put("sand", "Sand");
    });

    public I18nGenerator(PackOutput output) {
        super(output, FTBStuffNThings.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("ftbstuff.itemGroup.tab", "FTB Stuff & Things");
        add("ftbstuff.any_block", "Any Block");
        add("ftbstuff.efficiency", "Efficiency: %s");
        add("ftbstuff.temperature", "Temperature: %s");
        add("ftbstuff.fluid_usage", "Uses: %s mB");
        add("ftbstuff.temperature.normal", "Normal");
        add("ftbstuff.temperature.hot", "Hot");
        add("ftbstuff.temperature.superheated", "Superheated");
        add("ftbstuff.temperature.chilled", "Chilled");
        add("ftbstuff.processing_time", "Processing time: %d sec");
        add("ftbstuff.start_mix", "Mix");
        add("ftbstuff.stop_mix", "Stop");
        add("ftbstuff.temperature_source", "Temperature Source");
        add("ftbstuff.temperature_source.click", "Click to show Temperature Sources");
        add("ftbstuff.jar_status.ready", "Ready To Craft");
        add("ftbstuff.jar_status.no_recipe", "No Recipe");
        add("ftbstuff.jar_status.not_enough_resources", "Insufficient Resources");
        add("ftbstuff.jar_status.output_full", "Output Full");
        add("ftbstuff.jar_status.crafting", "Crafting!");
        add("ftbstuff.making", "Making:");
        add("ftbstuff.dripper.chance", "Chance: %s%% / sec");
        add("ftbstuff.dripper.consume_on_fail", "Fluid is consumed even on failed attempt");
        add("ftbstuff.tooltip.hammers", "Crushes materials down to their core components");
        add("ftbstuff.tooltip.energy", "Energy: %s FE");
        add("ftbstuff.tooltip.fluid", "Fluid: %smB %s");
        add("ftbstuff.crook.limit", "Max drops per block broken: %s");
        add("ftbstuff.sluice", "Sluice");
        add("ftbstuff.strainer.empty", "Water Strainer is empty!");
        add("ftbstuff.wrong_mesh", "This Mesh doesn't fit in this Sluice!");
        add("ftbstuff.jade.processing", "Processing");
        add("ftbstuff.jade.buffer", "Buffer");
        add("ftbstuff.jade.time_left", "Time Left: %s");
        add("ftbstuff.jei.loot_summary", "Loot Table Information");
        add("ftbstuff.jei.loot_summary.pool", "Pool: %s");
        add("ftbstuff.jei.loot_summary.pool_header", "Loot table has %d loot pools:");
        add("ftbstuff.jei.loot_summary.too_many", "%s more loot entries are not displayed");
        add("config.jade.plugin_ftbstuff.autohammer", "Autohammer");
        add("config.jade.plugin_ftbstuff.pump", "Pump");
        add("block.ftbstuff.fusing_machine.tooltip", "Used to fuse items together to produce new results");
        add("block.ftbstuff.super_cooler.tooltip", "Used to \"super\"-cool items to produce new results");
        add("death.attack.static_electric", "%1$s was killed by static electricity");
        add("ftbstuff.advancement.root", "FTB Stuff & Things");
        add("ftbstuff.advancement.root.desc", "Just a collection of useful stuff... and things");
        add("ftbstuff.advancement.supercharged", "Supercharged");
        add("ftbstuff.advancement.supercharged.desc", "Over-wind a Pump and suffer the consequences");
        add("ftbstuff.hold_shift", "Hold Shift for more info");
        add("ftbstuff.sluice.props.processing_time", "Processing Time: %sx");
        add("ftbstuff.sluice.props.fluid_usage", "Fluid Usage Multiplier: %sx");
        add("ftbstuff.sluice.props.tank", "Fluid Tank Capacity: %smB");
        add("ftbstuff.sluice.props.auto", "Allows automation of: %s | %s");
        add("ftbstuff.sluice.props.auto.fluid", "Fluids");
        add("ftbstuff.sluice.props.auto.item", "Items");
        add("ftbstuff.autohammer.waiting", "Waiting...");
        add("ftbstuff.autohammer.running", "Running");

        addBlock(BlocksRegistry.OAK_SLUICE, "Oak Sluice");
        addBlock(BlocksRegistry.SPRUCE_SLUICE, "Spruce Sluice");
        addBlock(BlocksRegistry.BIRCH_SLUICE, "Birch Sluice");
        addBlock(BlocksRegistry.JUNGLE_SLUICE, "Jungle Sluice");
        addBlock(BlocksRegistry.ACACIA_SLUICE, "Acacia Sluice");
        addBlock(BlocksRegistry.DARK_OAK_SLUICE, "Dark Oak Sluice");
        addBlock(BlocksRegistry.MANGROVE_SLUICE, "Mangrove Sluice");
        addBlock(BlocksRegistry.CHERRY_SLUICE, "Cherry Sluice");
        addBlock(BlocksRegistry.PALE_OAK_SLUICE, "Pale Oak Sluice");
        addBlock(BlocksRegistry.CRIMSON_SLUICE, "Crimson Sluice");
        addBlock(BlocksRegistry.WARPED_SLUICE, "Warped Sluice");

        addBlock(BlocksRegistry.IRON_SLUICE, "Iron Sluice");
        addBlock(BlocksRegistry.DIAMOND_SLUICE, "Diamond Sluice");
        addBlock(BlocksRegistry.NETHERITE_SLUICE, "Netherite Sluice");

        addBlock(BlocksRegistry.IRON_AUTO_HAMMER, "Iron Auto-Hammer");
        addBlock(BlocksRegistry.GOLD_AUTO_HAMMER, "Gold Auto-Hammer");
        addBlock(BlocksRegistry.DIAMOND_AUTO_HAMMER, "Diamond Auto-Hammer");
        addBlock(BlocksRegistry.NETHERITE_AUTO_HAMMER, "Netherite Auto-Hammer");

        addBlock(BlocksRegistry.STONE_COBBLESTONE_GENERATOR, "Stone Cobble Generator");
        addBlock(BlocksRegistry.IRON_COBBLESTONE_GENERATOR, "Iron Cobble Generator");
        addBlock(BlocksRegistry.GOLD_COBBLESTONE_GENERATOR, "Gold Cobble Generator");
        addBlock(BlocksRegistry.DIAMOND_COBBLESTONE_GENERATOR, "Diamond Cobble Generator");
        addBlock(BlocksRegistry.NETHERITE_COBBLESTONE_GENERATOR, "Netherite Cobble Generator");
        addBlock(BlocksRegistry.STONE_BASALT_GENERATOR, "Stone Basalt Generator");
        addBlock(BlocksRegistry.IRON_BASALT_GENERATOR, "Iron Basalt Generator");
        addBlock(BlocksRegistry.GOLD_BASALT_GENERATOR, "Gold Basalt Generator");
        addBlock(BlocksRegistry.DIAMOND_BASALT_GENERATOR, "Diamond Basalt Generator");
        addBlock(BlocksRegistry.NETHERITE_BASALT_GENERATOR, "Netherite Basalt Generator");

        addBlock(BlocksRegistry.FUSING_MACHINE, "SlowMelter 9000");
        addBlock(BlocksRegistry.SUPER_COOLER, "\"Super\" Cooler");

        addBlock(BlocksRegistry.PUMP, "Pump");

        addBlock(BlocksRegistry.DRIPPER, "Dripper");

        addBlock(BlocksRegistry.ACACIA_STRAINER, "Acacia Water Strainer");
        addBlock(BlocksRegistry.BAMBOO_STRAINER, "Bamboo Water Strainer");
        addBlock(BlocksRegistry.BIRCH_STRAINER, "Birch Water Strainer");
        addBlock(BlocksRegistry.CHERRY_STRAINER, "Cherry Water Strainer");
        addBlock(BlocksRegistry.CRIMSON_STRAINER, "Crimson Water Strainer");
        addBlock(BlocksRegistry.DARK_OAK_STRAINER, "Dark Oak Water Strainer");
        addBlock(BlocksRegistry.JUNGLE_STRAINER, "Jungle Water Strainer");
        addBlock(BlocksRegistry.MANGROVE_STRAINER, "Mangrove Water Strainer");
        addBlock(BlocksRegistry.OAK_STRAINER, "Oak Water Strainer");
        addBlock(BlocksRegistry.SPRUCE_STRAINER, "Spruce Water Strainer");
        addBlock(BlocksRegistry.WARPED_STRAINER, "Warped Water Strainer");
        addBlock(BlocksRegistry.BAMBOO_SLUICE, "Bamboo Sluice");

        addBlock(BlocksRegistry.TUBE, "Cast Iron Tube");
        addBlock(BlocksRegistry.JAR, "Glass Jar");
        addBlock(BlocksRegistry.TEMPERED_JAR, "Tempered Glass Jar");
        addBlock(BlocksRegistry.JAR_AUTOMATER, "Jar Automater");
        addBlock(BlocksRegistry.BLUE_MAGMA_BLOCK, "Blue Magma Block");
        addBlock(BlocksRegistry.CREATIVE_HOT_TEMPERATURE_SOURCE, "Creative Hot Temperature Source");
        addBlock(BlocksRegistry.CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE, "Creative Superheated Temperature Source");
        addBlock(BlocksRegistry.CREATIVE_CHILLED_TEMPERATURE_SOURCE, "Creative Chilled Temperature Source");

        addBlock(BlocksRegistry.CAST_IRON_BLOCK, "Cast Iron Block");

        addBlock(BlocksRegistry.DUST_BLOCK, "Dust");
        addBlock(BlocksRegistry.CRUSHED_BASALT, "Crushed Basalt");
        addBlock(BlocksRegistry.CRUSHED_ENDSTONE, "Crushed Endstone");
        addBlock(BlocksRegistry.CRUSHED_NETHERRACK, "Crushed Netherrack");

        addItem(ItemsRegistry.CLOTH_MESH, "Cloth Mesh");
        addItem(ItemsRegistry.IRON_MESH, "Iron Mesh");
        addItem(ItemsRegistry.GOLD_MESH, "Gold Mesh");
        addItem(ItemsRegistry.DIAMOND_MESH, "Diamond Mesh");
        addItem(ItemsRegistry.BLAZING_MESH, "Blazing Mesh");

        addItem(ItemsRegistry.FLUID_CAPSULE, "Fluid Capsule");
        addItem(ItemsRegistry.WATER_BOWL, "Water Bowl");

        addItem(ItemsRegistry.CAST_IRON_GEAR, "Cast Iron Gear");
        addItem(ItemsRegistry.CAST_IRON_INGOT, "Cast Iron Ingot");
        addItem(ItemsRegistry.CAST_IRON_NUGGET, "Cast Iron Nugget");
        addItem(ItemsRegistry.TEMPERED_GLASS, "Tempered Glass");

        addItem(ItemsRegistry.STONE_HAMMER, "Stone Hammer");
        addItem(ItemsRegistry.IRON_HAMMER, "Iron Hammer");
        addItem(ItemsRegistry.GOLD_HAMMER, "Gold Hammer");
        addItem(ItemsRegistry.DIAMOND_HAMMER, "Diamond Hammer");
        addItem(ItemsRegistry.NETHERITE_HAMMER, "Netherite Hammer");

        addItem(ItemsRegistry.CROOK, "Stone Crook");
        addItem(ItemsRegistry.STONE_ROD, "Stone Rod");

        BlocksRegistry.BARRELS.forEach((barrel) -> {
            String name = barrel.getId().getPath().split("_")[0];
            // Upper case first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1);

            addBlock(barrel, name + " Barrel");
        });

        addBlock(BlocksRegistry.CRATE, "Crate");
        addBlock(BlocksRegistry.SMALL_CRATE, "Small Crate");
        addBlock(BlocksRegistry.PULSATING_CRATE, "Pulsating Crate");

        BlocksRegistry.compressedBlockTranslations().forEach((id, translation) -> {
            List<DeferredBlock<Block>> compressedBlocks = BlocksRegistry.compressedBlocks(id);
            for (int lvl = 0; lvl < compressedBlocks.size(); lvl++) {
                DeferredBlock<Block> block = compressedBlocks.get(lvl);
                addBlock(block, compressedBlockName(translation, lvl));
            }
        });
    }

    private String compressedBlockName(String name, int level) {
        String prefix = level >= COMPRESSED_PREFIXES.length ? "???" : COMPRESSED_PREFIXES[level];
        return prefix + "Compressed " + name;
    }
}
