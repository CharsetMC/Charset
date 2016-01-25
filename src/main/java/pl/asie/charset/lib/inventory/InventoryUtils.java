package pl.asie.charset.lib.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public final class InventoryUtils {
	private InventoryUtils() {

	}

	public static IItemHandler getItemHandler(TileEntity tile, EnumFacing facing) {
		if (tile == null) {
			return null;
		}

		if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
			return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
		} else if (tile instanceof ISidedInventory) {
			IItemHandler handler = new SidedInvWrapper((ISidedInventory) tile, facing);
			return handler.getSlots() > 0 ? handler : null;
		} else if (tile instanceof IInventory) {
			return new InvWrapper((IInventory) tile);
		}

		return null;
	}
}
