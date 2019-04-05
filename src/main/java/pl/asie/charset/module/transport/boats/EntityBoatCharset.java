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

package pl.asie.charset.module.transport.boats;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

public class EntityBoatCharset extends EntityBoat {
	private ItemMaterial material;

	public EntityBoatCharset(World worldIn) {
		super(worldIn);
	}

	public EntityBoatCharset(World worldIn, double x, double y, double z, ItemMaterial material) {
		super(worldIn, x, y, z);
		this.material = material;
	}

	@Override
	public Item getItemBoat() {
		return Items.BOAT;
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return ItemBoatCharset.createStack(material);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		captureDrops = true;
		if (super.attackEntityFrom(source, amount)) {
			captureDrops = false;
			if (!world.isRemote) {
				for (EntityItem entityItem : capturedDrops) {
					if (entityItem.getItem().getItem() == Items.BOAT) {
						entityItem.setItem(ItemBoatCharset.createStack(material));
					}
					world.spawnEntity(entityItem);
				}
			}
			capturedDrops.clear();
			return true;
		} else {
			captureDrops = false;
			capturedDrops.clear();
			return false;
		}
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
		captureDrops = true;
		super.updateFallState(y, onGroundIn, state, pos);
		captureDrops = false;

		if (!world.isRemote) {
			ItemMaterial stickMaterial = material.getRelated("stick");
			if (stickMaterial == null) {
				stickMaterial = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Items.STICK));
			}

			for (EntityItem entityItem : capturedDrops) {
				if (entityItem.getItem().getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
					ItemStack newStack = material.getStack().copy();
					newStack.setCount(entityItem.getItem().getCount());
					entityItem.setItem(newStack);
				} else if (entityItem.getItem().getItem() == Items.STICK) {
					ItemStack newStack = stickMaterial.getStack().copy();
					newStack.setCount(entityItem.getItem().getCount());
					entityItem.setItem(newStack);
				}
				world.spawnEntity(entityItem);
			}
		}
		capturedDrops.clear();
	}
}
