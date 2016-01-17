package pl.asie.charset.storage.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by asie on 1/11/16.
 */
public class SlotArmorBackpack extends Slot {
	private final EntityPlayer player;

	public SlotArmorBackpack(EntityPlayer player, IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
		this.player = player;
	}

	public int getSlotStackLimit() {
		return 1;
	}

	public boolean isItemValid(ItemStack stack) {
		if (stack == null || stack.getItem() instanceof ItemBackpack) return false;
		return stack.getItem().isValidArmor(stack, 1, player);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		ItemStack stack = getStack();
		return stack == null || !(stack.getItem() instanceof ItemBackpack);
	}

	@SideOnly(Side.CLIENT)
	public String getSlotTexture() {
		return ItemArmor.EMPTY_SLOT_NAMES[1];
	}
}
