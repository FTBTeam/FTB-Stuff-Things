package dev.ftb.mods.ftbstuffnthings.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class CrookItem extends Item {
    public CrookItem(Properties properties) {
        super(properties.stacksTo(1).tool(
                ToolMaterial.STONE, BlockTags.MINEABLE_WITH_SHOVEL,
                2, -2.8F, 0F)
        );
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
    }
}
