package pl.asie.charset.storage.backpack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.storage.ModCharsetStorage;

/**
 * Created by asie on 1/11/16.
 */
public class HandlerBackpackUnequip {
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
				&& event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) == null
				&& event.getEntityPlayer().isSneaking()
				&& event.getFace() == EnumFacing.UP && !event.getWorld().isRemote) {
			ItemStack backpack = ItemBackpack.getBackpack(event.getEntityPlayer());
			if (backpack != null) {
				IBlockState sourceBlock = event.getWorld().getBlockState(event.getPos());
				if (sourceBlock.getBlock().isSideSolid(sourceBlock, event.getWorld(), event.getPos(), event.getFace())) {
					if (backpack.getItem().onItemUse(backpack, event.getEntityPlayer(), event.getWorld(), event.getPos(), EnumHand.MAIN_HAND, event.getFace(), 0, 0, 0) == EnumActionResult.SUCCESS) {
						event.setCanceled(true);
						event.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.CHEST, null);
						event.getEntityPlayer().inventoryContainer.detectAndSendChanges();
						TileEntity tile = event.getWorld().getTileEntity(event.getPos().up());
						if (tile instanceof TileBackpack) {
							((TileBackpack) tile).readFromItemStack(backpack);
						} else {
							ModCharsetStorage.logger.error("Something went wrong with placing backpack at " + event.getPos().toString() + "! Please report!");
						}
					}
				}
			}
		}
	}
}
