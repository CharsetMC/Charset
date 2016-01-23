package pl.asie.charset.storage.backpack;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;
import pl.asie.charset.lib.recipe.IDyeableItem;

public class ItemBackpack extends ItemBlock implements IDyeableItem {
	public class InventoryOwnerBackpack implements IInventoryOwner {
		public final ItemStack stack;

		protected InventoryOwnerBackpack(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public void onInventoryChanged(IInventory inventory) {
			((InventorySimple) inventory).writeToNBT(stack.getTagCompound(), "items");
		}
	}

	public ItemBackpack(Block block) {
		super(block);
	}

	public static ItemStack getBackpack(EntityPlayer player) {
		ItemStack stack = player.getCurrentArmor(2);
		if (stack != null && stack.getItem() instanceof ItemBackpack) {
			return stack;
		} else {
			return null;
		}
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return armorType == 1;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (entity instanceof EntityPlayer) {
			Container container = ((EntityPlayer) entity).inventoryContainer;
			Slot slot = container.getSlot(6);
			if (!(slot instanceof SlotArmorBackpack)) {
				SlotArmorBackpack newSlot = new SlotArmorBackpack((EntityPlayer) entity, slot.inventory,
						slot.getSlotIndex(), slot.xDisplayPosition, slot.yDisplayPosition);
				newSlot.slotNumber = slot.slotNumber;
				container.inventorySlots.set(6, newSlot);
			}
		}
	}

	@Override
	public int getColor(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("color") ? stack.getTagCompound().getInteger("color") : -1;
	}

	@Override
	public boolean hasColor(ItemStack stack) {
		return getColor(stack) >= 0;
	}

	@Override
	public void setColor(ItemStack stack, int color) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		stack.getTagCompound().setInteger("color", color);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		int color = getColor(stack);
		return color >= 0 ? color : BlockBackpack.DEFAULT_COLOR;
	}

	public IInventory getInventory(ItemStack stack) {
		if (stack.getItem() == this) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}

			InventorySimple inventory = new InventorySimple(27, new InventoryOwnerBackpack(stack));
			inventory.readFromNBT(stack.getTagCompound(), "items");
			return inventory;
		} else {
			return null;
		}
	}
}
