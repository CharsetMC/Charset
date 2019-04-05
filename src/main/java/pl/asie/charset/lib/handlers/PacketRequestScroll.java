/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.handlers;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.ItemUtils;

public class PacketRequestScroll extends Packet {
	private int currPos;
	private int wheel;

	public PacketRequestScroll(int currPos, int wheel) {
		this.currPos = currPos;
		this.wheel = wheel;
	}

	public PacketRequestScroll() {

	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		this.currPos = buf.readUnsignedByte();
		this.wheel = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		EntityPlayer player = getPlayer(handler);
		ItemStack currStack = player.inventory.getStackInSlot(currPos);
		ShiftScrollHandler.Provider provider = ShiftScrollHandler.INSTANCE.getMatchingProvider(currStack);
		if (!currStack.isEmpty() && provider != null) {
			if (player.isCreative()) {
				NonNullList<ItemStack> stacks = NonNullList.create();
				provider.addAllMatching(stacks);

				int id = -1;

				for (int i = 0; i < stacks.size(); i++) {
					ItemStack compStack = stacks.get(i);
					if (ItemUtils.equals(currStack, compStack, false, currStack.getHasSubtypes(), true)) {
						id = i;
						break;
					}
				}

				if (id < 0) {
					for (int i = 0; i < stacks.size(); i++) {
						ItemStack compStack = stacks.get(i);
						if (ItemUtils.equals(currStack, compStack, false, currStack.getHasSubtypes(), false)) {
							id = i;
							break;
						}
					}
				}

				if (id >= 0) {
					int newPos = (id + (wheel < 0 ? 1 : -1)) % stacks.size();
					while (newPos < 0) newPos += stacks.size();
					ItemStack newStack = stacks.get(newPos).copy();
					newStack.setCount(currStack.getCount());
					player.setHeldItem(EnumHand.MAIN_HAND, newStack);
					return;
				}
			} else {
				NonNullList<ItemStack> mainInv = player.inventory.mainInventory;
				TIntList intList = new TIntArrayList(mainInv.size());
				for (int i = 0; i < mainInv.size(); i++) {
					int pos = (player.inventory.currentItem + (wheel < 0 ? -i : i)) % mainInv.size();
					while (pos < 0) pos += mainInv.size();
					ItemStack compStack = mainInv.get(pos);
					if (provider.matches(compStack)) {
						intList.add(pos);
					}
				}

				if (intList.size() >= 2) {
					ItemStack temp = null;
					for (int i = 0; i < intList.size() + 1; i++) {
						int pos = intList.get(i % intList.size());
						if (temp == null) {
							temp = mainInv.get(pos);
						} else {
							ItemStack target = mainInv.get(pos);
							player.inventory.setInventorySlotContents(pos, temp);
							temp = target;
						}
					}
				}
			}
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeByte(currPos);
		buf.writeInt(wheel);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
