/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.storage.backpack;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
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

	public static class Color implements IItemColor {
		@Override
		@SideOnly(Side.CLIENT)
		public int getColorFromItemstack(ItemStack stack, int renderPass) {
			int color = ((IDyeableItem) stack.getItem()).getColor(stack);
			return color >= 0 ? color : BlockBackpack.DEFAULT_COLOR;
		}
	}

	public ItemBackpack(Block block) {
		super(block);
		setMaxStackSize(1);
	}

	public static ItemStack getBackpack(EntityPlayer player) {
		ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (stack != null && stack.getItem() instanceof ItemBackpack) {
			return stack;
		} else {
			return null;
		}
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
		return armorType == EntityEquipmentSlot.CHEST;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (entity instanceof EntityPlayer) {
			Container container = ((EntityPlayer) entity).inventoryContainer;
			Slot slot = container.getSlot(6);
			if (!(slot instanceof SlotBackpack)) {
				SlotBackpack newSlot = new SlotBackpack((EntityPlayer) entity, slot.inventory,
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
