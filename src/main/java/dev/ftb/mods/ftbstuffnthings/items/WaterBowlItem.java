package dev.ftb.mods.ftbstuffnthings.items;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.UnknownNullability;

public class WaterBowlItem extends Item {
	protected static final int BOWL_CAPACITY = FluidType.BUCKET_VOLUME / 4;

	public WaterBowlItem(Properties properties) {
		super(properties
				.stacksTo(1)
				.component(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY));
//				.component(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.copyOf(new FluidStack(Fluids.WATER, BOWL_CAPACITY))));
	}

	public static boolean fillBowl(Level level, Player player) {
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

		if (hit.getType() == HitResult.Type.BLOCK && level.getBlockState(hit.getBlockPos()).getBlock() == Blocks.WATER) {
			player.awardStat(Stats.ITEM_USED.get(Items.BOWL));
			player.playSound(SoundEvents.BUCKET_FILL, 1F, 1F);
			return true;
		}

		return false;
	}

	public static class WaterBowlFluidHandler extends ItemAccessFluidHandler {
		public WaterBowlFluidHandler(ItemAccess container) {
			super(container, ComponentsRegistry.STORED_FLUID.get(), BOWL_CAPACITY);
		}

		@Override
		protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
			return ItemResource.of(Items.BOWL);
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			return amount >= BOWL_CAPACITY && resource.getFluid() == Fluids.WATER ?
					super.extract(index, resource, amount, transaction) :
					0;
		}
	}

	@EventBusSubscriber(modid = FTBStuffNThings.MOD_ID)
	public static class Listener {
		@SubscribeEvent
		public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
			Player player = event.getEntity();
			if (event.getItemStack().getItem() == Items.BOWL && WaterBowlItem.fillBowl(event.getLevel(), player)) {
				event.getItemStack().shrink(1);

				if (!event.getLevel().isClientSide()) {
					if (event.getItemStack().isEmpty()) {
						player.setItemInHand(event.getHand(), ItemsRegistry.WATER_BOWL.toStack());
					} else {
						player.getInventory().placeItemBackInInventory(ItemsRegistry.WATER_BOWL.toStack());
					}
				}

				player.swing(event.getHand());
				event.setCancellationResult(player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
				event.setCanceled(true);
			}
		}
	}
}
