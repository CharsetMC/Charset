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

package pl.asie.charset.module.transport.boats;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;

public class ItemBoatCharset extends ItemBoat {
	protected static final ThreadLocal<ItemStack> STACK = new ThreadLocal<>();

	public ItemBoatCharset() {
		super(EntityBoat.Type.OAK);
	}

	protected static ItemMaterial getMaterial(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("material")) {
			return ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material");
		} else {
			return ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.PLANKS, 1, 0));
		}
	}

	protected static ItemStack createStack(ItemMaterial material) {
		if (material.getStack().getItem() == getItemFromBlock(Blocks.PLANKS)) {
			switch (material.getStack().getItemDamage()) {
				case 0:
					return new ItemStack(Items.BOAT);
				case 1:
					return new ItemStack(Items.SPRUCE_BOAT);
				case 2:
					return new ItemStack(Items.BIRCH_BOAT);
				case 3:
					return new ItemStack(Items.JUNGLE_BOAT);
				case 4:
					return new ItemStack(Items.ACACIA_BOAT);
				case 5:
					return new ItemStack(Items.DARK_OAK_BOAT);
			}
		}

		ItemStack stack = new ItemStack(CharsetTransportBoats.itemBoat);
		material.writeToNBT(ItemUtils.getTagCompound(stack, true), "material");
		return stack;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (ItemMaterial material : ItemMaterialRegistry.INSTANCE.getMaterialsByType("plank")) {
				if (material.getStack().getItem() != Item.getItemFromBlock(Blocks.PLANKS)) {
					items.add(createStack(material));
				}
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		STACK.set(playerIn.getHeldItem(handIn));
		ActionResult<ItemStack> result = super.onItemRightClick(worldIn, playerIn, handIn);
		STACK.set(null);
		return result;
	}
}
