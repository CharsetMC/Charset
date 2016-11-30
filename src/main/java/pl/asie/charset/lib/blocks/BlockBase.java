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

package pl.asie.charset.lib.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.storage.ModCharsetStorage;
import pl.asie.charset.storage.barrel.TileEntityDayBarrel;

import java.util.*;

public abstract class BlockBase extends Block {
	protected final Map<IBlockAccess, TileEntity> lastBrokenMap = new HashMap<>();
	private boolean isTileProvider = this instanceof ITileEntityProvider;
	private ImmutableList<ItemStack> items;

	public BlockBase(Material materialIn) {
		super(materialIn);
	}

	protected TileEntity getTileAfterBreak(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile == null) {
			TileEntity lastBroken = lastBrokenMap.get(world);
			if (lastBroken != null && lastBroken.getPos().equals(pos)) {
				tile = lastBroken;
			}
		}
		return tile;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (isTileProvider) {
			TileEntity tile = worldIn.getTileEntity(pos);

			if (tile instanceof TileBase) {
				lastBrokenMap.put(worldIn, tile);
				((TileBase) tile).dropContents();
			}
		}

		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				return ((TileBase) tile).getPickedBlock();
			}
		}

		return new ItemStack(this);
	}

	protected Collection<ItemStack> getCreativeItems() {
		return ImmutableList.of(new ItemStack(this));
	}

	protected List<Collection<ItemStack>> getCreativeItemSets() {
		return Collections.EMPTY_LIST;
	}

	protected int getCreativeItemSetAmount() {
		return 1;
	}

	@Override
	public void getSubBlocks(Item me, CreativeTabs tab, NonNullList<ItemStack> itemList) {
		if (items == null) {
			ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
			builder.addAll(getCreativeItems());
			List<Collection<ItemStack>> sets = getCreativeItemSets();

			if (sets.size() > 0) {
				if (ModCharsetLib.INDEV) {
					for (Collection<ItemStack> set : sets)
						builder.addAll(set);
				} else {
					Calendar cal = ModCharsetLib.calendar.get();
					int doy = cal.get(Calendar.DAY_OF_YEAR) - 1 /* start at 0, not 1 */;
					Collections.shuffle(sets, new Random(doy));
					for (int i = 0; i < Math.min(getCreativeItemSetAmount(), sets.size()); i++)
						builder.addAll(sets.get(i));
				}
			}

			items = builder.build();
		}

		itemList.addAll(items);
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		if (isTileProvider) {
			TileEntity tile = worldIn.getTileEntity(pos);

			if (tile instanceof TileBase) {
				return ((TileBase) tile).getComparatorValue();
			}
		}
		return 0;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				((TileBase) tile).onPlacedBy(placer, stack);
			}
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (isTileProvider) {
			List<ItemStack> stacks = new ArrayList<ItemStack>();

			TileEntity tile = getTileAfterBreak(world, pos);
			if (tile instanceof TileBase) {
				stacks.add(((TileBase) tile).getDroppedBlock());
				return stacks;
			}
		}

		return super.getDrops(world, pos, state, fortune);
	}
}
