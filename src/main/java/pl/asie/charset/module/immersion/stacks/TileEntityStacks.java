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

package pl.asie.charset.module.immersion.stacks;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;

import javax.annotation.Nullable;

public class TileEntityStacks extends TileBase {
	protected final ItemStack[] stacks = new ItemStack[64];

	public TileEntityStacks() {

	}

	public static boolean canAcceptStackType(ItemStack stack) {
		return StackShapes.isIngot(stack) || StackShapes.isFlatPlaced(stack);
	}

	private Vec3d getCenter(int i) {
		return StackShapes.getIngotBox(i, stacks[i]).getCenter();
	}

	protected boolean canPlace(int i, ItemStack stack) {
		if (stacks[i] != null) {
			return false;
		}

		ItemStack opponent = stacks[i ^ 1];
		if (opponent != null) {
			if (StackShapes.isIngot(opponent) && !StackShapes.isIngot(stack)) {
				return false;
			}
			if (StackShapes.isFlatPlaced(opponent) && !StackShapes.isFlatPlaced(stack)) {
				return false;
			}
		}

		if (StackShapes.isFlatPlaced(stack) && ((i & 1) == 1) && stacks[i & (~1)] == null) {
			return false;
		}

		if (i >= 8) {
			return (stacks[i - 8] != null && stacks[(i ^ 1) - 8] != null);
		} else {
			TileEntity bottom = world.getTileEntity(pos.down());
			if (bottom instanceof TileEntityStacks) {
				return (((TileEntityStacks) bottom).stacks[i + 56] != null && ((TileEntityStacks) bottom).stacks[(i ^ 1) + 56] != null);
			} else {
				return world.isSideSolid(pos.down(), EnumFacing.UP, false);
			}
		}
	}

	protected boolean canRemove(int i) {
		if (stacks[i] == null) {
			return false;
		}

		if (((i & 1) == 0) && stacks[i | 1] != null && StackShapes.isFlatPlaced(stacks[i | 1])) {
			return false;
		}

		if (i < 56) {
			return (stacks[i + 8] == null && stacks[(i ^ 1) + 8] == null);
		} else {
			TileEntity top = world.getTileEntity(pos.up());
			if (top instanceof TileEntityStacks) {
				return (((TileEntityStacks) top).stacks[i - 56] == null && ((TileEntityStacks) top).stacks[(i ^ 1) - 56] == null);
			} else {
				return true;
			}
		}
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
			if (stacks[i] == null && canPlace(i, stack)) {
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
		markChunkDirty();
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
			markChunkDirty();
			markBlockForUpdate();
			return stack;
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);

		for (int i = 0; i < 32; i++) {
			if (compound.hasKey("s" + i, Constants.NBT.TAG_COMPOUND)) {
				int offset = i * 2;
				NBTTagCompound cpd = compound.getCompoundTag("s" + i);
				if (cpd.hasKey("a", Constants.NBT.TAG_COMPOUND)) {
					stacks[offset] = new ItemStack(cpd.getCompoundTag("a"));
					if (stacks[offset].isEmpty()) {
						stacks[offset] = null;
					}
				}
				offset += 1;
				if (cpd.hasKey("b", Constants.NBT.TAG_COMPOUND)) {
					stacks[offset] = new ItemStack(cpd.getCompoundTag("b"));
					if (stacks[offset].isEmpty()) {
						stacks[offset] = null;
					}
				}
			} else {
				stacks[i * 2] = null;
				stacks[i * 2 + 1] = null;
			}
		}
		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		for (int i = 0; i < 64; i+=2) {
			if (stacks[i] != null || stacks[i + 1] != null) {
				NBTTagCompound cpd1 = new NBTTagCompound();
				if (stacks[i] != null) {
					stacks[i].writeToNBT(cpd1);
				}
				NBTTagCompound cpd2 = new NBTTagCompound();
				if (stacks[i + 1] != null) {
					stacks[i + 1].writeToNBT(cpd2);
				}
				NBTTagCompound cpd = new NBTTagCompound();
				if (!cpd1.hasNoTags()) {
					cpd.setTag("a", cpd1);
				}
				if (!cpd2.hasNoTags()) {
					cpd.setTag("b", cpd2);
				}
				compound.setTag("s" + (i >> 1), cpd);
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
