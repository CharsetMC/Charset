/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.storage.chests;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.*;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.List;
import java.util.Optional;

public class ItemBlockChestCharset extends ItemBlockBase {
	public ItemBlockChestCharset(Block block) {
		super(block);
		setTranslationKey("chest");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		Optional<String> s = ItemMaterialRegistry.INSTANCE.getLocalizedNameFor(
				ItemMaterialRegistry.INSTANCE.getMaterial(is.getTagCompound(), "wood")
		);

		String baseName = I18n.translateToLocal(getTranslationKey(is) + ".name");

		return s.map(s1 -> I18n.translateToLocalFormatted("tile.charset.chest.format", s1, baseName)).orElse(baseName);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		TileEntityChestCharset chest = (TileEntityChestCharset) newState.getBlock().createTileEntity(world, newState);
		chest.setWorld(world);
		chest.setPos(pos);
		chest.loadFromStack(stack);

		BlockPos neighborPos = null;
		EnumFacing neighborFacing = null;
		TileEntityChestCharset neighbor = null;

		if (player.isSneaking()) {
			if (side.getAxis() != EnumFacing.Axis.Y) {
				BlockPos otherPos = pos.offset(side.getOpposite());
				TileEntity otherTile = world.getTileEntity(otherPos);
				if (otherTile instanceof TileEntityChestCharset
						&& ((TileEntityChestCharset) otherTile).material.getMaterial() == chest.material.getMaterial()) {
					if (((TileEntityChestCharset) otherTile).hasNeighbor()) {
						return false;
					}

					neighborPos = otherPos;
					neighborFacing = side.getOpposite();
					neighbor = (TileEntityChestCharset) otherTile;
				}
			}
		} else {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos otherPos = pos.offset(facing);
				TileEntity otherTile = world.getTileEntity(otherPos);
				if (otherTile instanceof TileEntityChestCharset
						&& ((TileEntityChestCharset) otherTile).material.getMaterial() == chest.material.getMaterial()) {
					if (neighborPos == null && !((TileEntityChestCharset) otherTile).hasNeighbor()) {
						neighborPos = otherPos;
						neighborFacing = facing;
						neighbor = (TileEntityChestCharset) otherTile;
					}
				}
			}
		}

		if (neighborPos != null) {
			EnumFacing selfFacing = player.getHorizontalFacing().getOpposite();

			if (neighborFacing.getAxis() == selfFacing.getAxis()) {
				selfFacing = selfFacing.rotateY();
			}

			world.setBlockState(neighborPos, world.getBlockState(neighborPos).withProperty(Properties.FACING4, selfFacing), 2);
			newState = newState.withProperty(Properties.FACING4, selfFacing);
		}

		if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
			if (neighborPos != null) {
				neighbor.setNeighbor((TileEntityChestCharset) world.getTileEntity(pos), neighborFacing.getOpposite());
			}
			return true;
		} else {
			return false;
		}
	}
}
