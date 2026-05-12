package dev.ftb.mods.ftbstuffnthings.items;

import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.function.Consumer;

public class HammerItem extends Item {
    public HammerItem(ToolMaterial material) {
        super(new Properties().stacksTo(1).tool(
                material, FTBStuffTags.Blocks.MINEABLE_WITH_HAMMER,
                1F, -2.8F, 0F)
        );
    }

    @Override
    public float getDestroySpeed(ItemStack arg, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? super.getDestroySpeed(arg, state) : 1.0f;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("ftbstuff.tooltip.hammers").withStyle(ChatFormatting.GRAY));
    }

//    @Override
//    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
//        return !state.is(getTier().getIncorrectBlocksForDrops());
//    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return /*ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility) ||*/ ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility);
    }
}
