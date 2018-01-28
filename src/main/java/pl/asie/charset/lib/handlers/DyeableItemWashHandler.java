package pl.asie.charset.lib.handlers;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.item.IDyeableItem;

public class DyeableItemWashHandler {
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
		if (!event.getWorld().isRemote && !event.getEntityPlayer().isSneaking()) {
			ItemStack stack = event.getEntityPlayer().getHeldItem(event.getHand());
			if (!stack.isEmpty() && stack.getItem() instanceof IDyeableItem) {
				IBlockState state = event.getWorld().getBlockState(event.getPos());
				if (state.getBlock() instanceof BlockCauldron
						&& state.getPropertyKeys().contains(BlockCauldron.LEVEL)) {
					event.setCanceled(true);

					int level = state.getValue(BlockCauldron.LEVEL);
					if (level > 0 && ((IDyeableItem) stack.getItem()).hasColor(stack)) {
						if (((IDyeableItem) stack.getItem()).removeColor(stack)) {
							event.getWorld().setBlockState(event.getPos(), state.withProperty(BlockCauldron.LEVEL, level - 1));
							event.getEntityPlayer().addStat(StatList.ARMOR_CLEANED);
						}
					}
				}
			}
		}
	}
}
