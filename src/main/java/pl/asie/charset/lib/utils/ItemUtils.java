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

package pl.asie.charset.lib.utils;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.CharsetLib;

import javax.annotation.Nullable;
import java.util.Optional;

public final class ItemUtils {
	private ItemUtils() {

	}

	public static ItemStack firstNonEmpty(ItemStack... stacks) {
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty())
				return stack;
		}

		return ItemStack.EMPTY;
	}

	public static int hashCode(ItemStack stack, boolean includeNBT) {
		if (stack.isEmpty())
			return 0;

		int hash = stack.getCount();
		hash = 31 * hash + Item.getIdFromItem(stack.getItem());
		hash = 7 * hash + stack.getItemDamage();
		if (includeNBT) {
			hash = 7 * hash + (stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0);
		}
		return hash;
	}

	public static boolean isOreType(ItemStack stack, String ore) {
		int oreId = OreDictionary.getOreID(ore);
		for (int i : OreDictionary.getOreIDs(stack)) {
			if (oreId == i)
				return true;
		}

		return false;
	}

	public static double getAttributeValue(EntityEquipmentSlot slot, ItemStack is, IAttribute attr) {
		Multimap<String, AttributeModifier> attrs = is.getItem().getAttributeModifiers(slot, is);
		if (attrs != null) {
			AttributeMap map = new AttributeMap();
			map.applyAttributeModifiers(attrs);
			IAttributeInstance instance = map.getAttributeInstance(attr);
			if (instance != null) {
				return instance.getAttributeValue();
			}
		}

		return 0;
	}

	public static NBTTagCompound getTagCompound(ItemStack stack, boolean create) {
		if (create && !stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		return stack.getTagCompound();
	}

	public static void writeToNBT(ItemStack stack, NBTTagCompound compound, String key) {
		NBTTagCompound compound1 = new NBTTagCompound();
		stack.writeToNBT(compound1);
		compound.setTag(key, compound1);
	}

	public static IBlockState getBlockState(ItemStack stack) {
		if (stack.getItem() instanceof ItemBlock) {
			int m = stack.getMetadata();
			if (m >= 0 && m < 16) {
				try {
					return ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
				} catch (Exception e) {
					return ((ItemBlock) stack.getItem()).getBlock().getDefaultState();
				}
			} else {
				return ((ItemBlock) stack.getItem()).getBlock().getDefaultState();
			}
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
		if (source.isEmpty()) {
			return target.isEmpty();
		}
		return equals(source, target, false, !source.getItem().isDamageable(), false);
	}

	public static boolean equals(ItemStack source, ItemStack target, boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
		return equals(source, target, matchStackSize, matchDamage, matchNBT, matchNBT);
	}

	public static boolean equals(ItemStack source, ItemStack target, boolean matchStackSize, boolean matchDamage, boolean matchNBT, boolean matchCaps) {
		if (source == target) {
			return true;
		} else if (source.isEmpty()) {
			return target.isEmpty();
		} else {
			if (source.getItem() != target.getItem()) {
				return false;
			}

			if (matchStackSize && source.getCount() != target.getCount()) {
				return false;
			}

			if (matchDamage && source.getItemDamage() != target.getItemDamage()) {
				return false;
			}

			if (matchNBT) {
				if (source.hasTagCompound() != target.hasTagCompound()) {
					return false;
				} else if (source.hasTagCompound() && !source.getTagCompound().equals(target.getTagCompound())) {
					return false;
				}
			}

			if (matchCaps) {
				if (!source.areCapsCompatible(target)) {
					return false;
				}
			}

			return true;
		}
	}

	public static EntityItem giveOrSpawnItemEntity(EntityPlayer player, World world, Vec3d loc, ItemStack stack, float mXm, float mYm, float mZm, float randomness, boolean emitEquipSound) {
		EntityItem entityItem = createItemEntity(world, loc, stack, mXm, mYm, mZm, randomness);
		if (!CharsetLib.alwaysDropDroppablesGivenToPlayer && player.inventory != null && !EntityUtils.isPlayerFake(player)) {
			EntityItemPickupEvent event = new EntityItemPickupEvent(player, entityItem);
			if (!MinecraftForge.EVENT_BUS.post(event) && event.getResult() != Event.Result.DENY) {
				if (emitEquipSound ? player.addItemStackToInventory(stack) : player.inventory.addItemStackToInventory(stack)) {
					return null;
				}
			}
		}

		world.spawnEntity(entityItem);
		return entityItem;
	}

	private static EntityItem createItemEntity(World world, Vec3d loc, ItemStack stack, float mXm, float mYm, float mZm, float randomness) {
		EntityItem entityItem = new EntityItem(world, loc.x, loc.y, loc.z, stack);
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
		return entityItem;
	}

	public static EntityItem spawnItemEntity(World world, Vec3d loc, ItemStack stack, float mXm, float mYm, float mZm, float randomness) {
		EntityItem entityItem = createItemEntity(world, loc, stack, mXm, mYm, mZm, randomness);
		world.spawnEntity(entityItem);
		return entityItem;
	}
}
