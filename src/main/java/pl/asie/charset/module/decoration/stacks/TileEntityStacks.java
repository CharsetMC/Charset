/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.decoration.stacks;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.Trait;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityStacks extends TileBase {
	protected final ItemStack[] stacks = new ItemStack[64];

	public TileEntityStacks() {

	}

	public static boolean canAcceptStackType(ItemStack stack) {
		ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
		if (material == null || !(material.getTypes().contains("ingot"))) {
			return false;
		}

		return true;
	}

	private Vec3d getCenter(int i) {
		int y = (i >> 2) & (~1);
		int x, z;
		if ((y & 2) == 2) {
			if ((i & 7) >= 2 && (i & 7) <= 5) {
				// swap 2..3 with 4..5
				i = (i & 1) | (6 - (i & 6)) | (i & (~7));
			}

			z = (((i & 1) | ((i >> 1) & 2)) * 4) + 2;
			x = ((i & 2) * 4) + 4;
		} else {
			x = (((i & 1) | ((i >> 1) & 2)) * 4) + 2;
			z = ((i & 2) * 4) + 4;
		}
		return new Vec3d(x / 16f, y / 16f, z / 16f);
	}

	protected boolean canPlace(int i) {
		return i < 8 || (stacks[i - 8] != null && stacks[(i ^ 1) - 8] != null);
	}

	protected boolean canRemove(int i) {
		return i >= 56 || (stacks[i + 8] == null && stacks[(i ^ 1) + 8] == null);
	}

	private void sort(IntList list, @Nullable Vec3d hitPos) {
		if (hitPos != null) {
			final Vec3d hitPosReal = hitPos.subtract(pos.getX(), pos.getY(), pos.getZ());
			list.sort((a, b) -> {
				double aDist = hitPosReal.squareDistanceTo(getCenter(a));
				double bDist = hitPosReal.squareDistanceTo(getCenter(b));
				return Double.compare(aDist, bDist);
			});
		}
	}

	public boolean offerStack(ItemStack stack, @Nullable Vec3d hitPos, boolean fillLayerFirst) {
		if (!canAcceptStackType(stack) || stack.getCount() != 1) {
			return false;
		}

		IntList freePositions = new IntArrayList();
		int firstPos = -1;
		for (int i = 0; i < 64; i++) {
			if (stacks[i] == null && canPlace(i)) {
				freePositions.add(i);
				if (firstPos < 0) {
					firstPos = i;
				}
			}

			if (fillLayerFirst && firstPos >= 0 && (i & 7) == 7) {
				break;
			}
		}

		if (freePositions.isEmpty()) {
			return false;
		}

		sort(freePositions, hitPos);
		stacks[freePositions.getInt(0)] = stack;
		markBlockForUpdate();
		return true;
	}

	public ItemStack removeStack(boolean simulate, @Nullable Vec3d hitPos) {
		IntList remPositions = new IntArrayList();
		for (int i = 0; i < 64; i++) {
			if (stacks[i] != null && canRemove(i)) {
				remPositions.add(i);
			}
		}

		if (remPositions.isEmpty()) {
			return ItemStack.EMPTY;
		}

		sort(remPositions, hitPos);
		if (simulate) {
			return stacks[remPositions.getInt(0)];
		} else {
			ItemStack stack = stacks[remPositions.getInt(0)];
			stacks[remPositions.getInt(0)] = null;
			markBlockForUpdate();
			return stack;
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);

		for (int i = 0; i < 64; i++) {
			if (compound.hasKey("s" + i, Constants.NBT.TAG_COMPOUND)) {
				stacks[i] = new ItemStack(compound.getCompoundTag("s" + i));
			} else {
				stacks[i] = null;
			}
		}
		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		for (int i = 0; i < 64; i++) {
			if (stacks[i] != null) {
				NBTTagCompound cpd = new NBTTagCompound();
				stacks[i].writeToNBT(cpd);
				compound.setTag("s" + i, cpd);
			}
		}
		return compound;
	}

	public boolean isFull() {
		for (int i = 0; i < 64; i++) {
			if (stacks[i] == null) {
				return false;
			}
		}

		return true;
	}

	public boolean isEmpty() {
		for (int i = 0; i < 64; i++) {
			if (stacks[i] != null) {
				return false;
			}
		}

		return true;
	}
}
