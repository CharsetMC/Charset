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

import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;

import pl.asie.charset.lib.TileBase;
import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;
import pl.asie.charset.storage.ModCharsetStorage;

public class TileBackpack extends TileBase implements IInteractionObject, IInventory, IInventoryOwner {
	private InventorySimple inventory = new InventorySimple(27, this);
	private int color = -1;

	public int getColor() {
		return color >= 0 ? color : BlockBackpack.DEFAULT_COLOR;
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerBackpack(this, playerInventory);
	}

	@Override
	public String getGuiID() {
		return null;
	}

	public void readFromItemStack(ItemStack stack) {
		readNBTData(stack.getTagCompound(), false);
	}

	public ItemStack writeToItemStack() {
		ItemStack stack = new ItemStack(ModCharsetStorage.backpackBlock);
		stack.setTagCompound(new NBTTagCompound());
		writeNBTData(stack.getTagCompound(), false);
		return stack;
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		int oldColor = color;
		color = nbt.hasKey("color") ? nbt.getInteger("color") : -1;
		if (!isClient) {
			inventory.readFromNBT(nbt, "items");
		} else if (oldColor != color) {
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		if (color >= 0) {
			nbt.setInteger("color", color);
		}
		if (!isClient) {
			inventory.writeToNBT(nbt, "items");
		}
		return nbt;
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inventory.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inventory.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inventory.setInventorySlotContents(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		inventory.openInventory(player);
		if (inventory.watchers.size() == 1) {
			worldObj.playSound( getPos().getX() + 0.5f, getPos().getY() + 0.5f, getPos().getZ() + 0.5f,
					SoundType.SNOW.getStepSound(), SoundCategory.BLOCKS, 1.0f, 0.6f, false);
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		inventory.closeInventory(player);
		if (inventory.watchers.size() == 0) {
			worldObj.playSound(getPos().getX() + 0.5f, getPos().getY() + 0.5f, getPos().getZ() + 0.5f,
					SoundType.SNOW.getStepSound(), SoundCategory.BLOCKS, 0.8f, 0.4f, false);
		}
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return inventory.isItemValidForSlot(index, stack);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public String getName() {
		return "Backpack";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation("tile.charset.backpack.name");
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		markDirty();
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}
}
