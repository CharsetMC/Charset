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

package pl.asie.charset.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ItemUtils {
	private ItemUtils() {

	}

	public static NBTTagCompound getTagCompound(ItemStack stack, boolean create) {
		if (create && !stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		return stack.getTagCompound();
	}

	public static void writeToNBT(ItemStack stack, NBTTagCompound compound, String key) {
		NBTTagCompound compound1 = new NBTTagCompound();
		if (stack != null) {
			stack.writeToNBT(compound1);
		}
		compound.setTag(key, compound1);
	}

	public static IBlockState getBlockState(ItemStack stack) {
		if (stack == null) {
			return Blocks.AIR.getDefaultState();
		} else {
			Block block = Block.getBlockFromItem(stack.getItem());
			if (block == null) {
				return Blocks.AIR.getDefaultState();
			} else {
				return block.getDefaultState();
			}
		}
	}

	public static boolean canMerge(ItemStack source, ItemStack target) {
		return equals(source, target, false, true, true);
	}

	public static boolean equalsMeta(ItemStack source, ItemStack target) {
		if (source == null || target == null) {
			return source == target;
		}
		return equals(source, target, false, !source.getItem().isDamageable(), false);
	}

	public static boolean equals(ItemStack source, ItemStack target, boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
		if (source == target) {
			return true;
		} else if (source == null || target == null) {
			return false;
		} else {
			if (source.getItem() != target.getItem()) {
				return false;
			}

			if (matchStackSize && source.stackSize != target.stackSize) {
				return false;
			}

			if (matchDamage && source.getItemDamage() != target.getItemDamage()) {
				return false;
			}

			if (matchNBT) {
				if (!ItemStack.areItemStackTagsEqual(source, target)) {
					return false;
				}
			}

			return true;
		}
	}

	public static EntityItem spawnItemEntity(World world, Vec3d loc, ItemStack stack, float mXm, float mYm, float mZm, float randomness) {
		EntityItem entityItem = new EntityItem(world, loc.xCoord, loc.yCoord, loc.zCoord, stack);
		entityItem.setDefaultPickupDelay();
		if (randomness <= 0.0f) {
			entityItem.motionX = mXm;
			entityItem.motionY = mYm;
			entityItem.motionZ = mZm;
		} else {
			entityItem.motionX = ((1.0f - randomness) + (((world.rand.nextDouble() - 0.5) * 2.0f) * randomness)) * mXm;
			entityItem.motionY = ((1.0f - randomness) + (((world.rand.nextDouble() - 0.5) * 2.0f) * randomness)) * mYm;
			entityItem.motionZ = ((1.0f - randomness) + (((world.rand.nextDouble() - 0.5) * 2.0f) * randomness)) * mZm;
		}
		world.spawnEntityInWorld(entityItem);
		return entityItem;
	}
}
