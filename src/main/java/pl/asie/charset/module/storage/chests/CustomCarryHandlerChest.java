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

package pl.asie.charset.module.storage.chests;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.carry.CustomCarryHandler;
import pl.asie.charset.api.carry.ICarryHandler;
import pl.asie.charset.lib.Properties;

public class CustomCarryHandlerChest extends CustomCarryHandler {
	public CustomCarryHandlerChest(ICarryHandler handler) {
		super(handler);
	}

	@Override
	public void onPlace(World world, BlockPos pos, EnumFacing side, EntityLivingBase player) {
		super.onPlace(world, pos, side, player);

		if (world.isRemote) {
			return;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileEntityChestCharset)) {
			return;
		}
		TileEntityChestCharset chest = (TileEntityChestCharset) tile;
		// invalidate old neighbor first
		chest.setNeighbor(null, null);

		if (player.isSneaking()) {
			BlockPos neighborPos = null;
			EnumFacing neighborFacing = null;
			TileEntityChestCharset neighbor = null;

			if (side.getAxis() != EnumFacing.Axis.Y) {
				BlockPos otherPos = pos.offset(side.getOpposite());
				TileEntity otherTile = world.getTileEntity(otherPos);
				if (otherTile instanceof TileEntityChestCharset
						&& ((TileEntityChestCharset) otherTile).material.getMaterial() == chest.material.getMaterial()) {
					if (((TileEntityChestCharset) otherTile).hasNeighbor()) {
						return;
					}

					neighborPos = otherPos;
					neighborFacing = side.getOpposite();
					neighbor = (TileEntityChestCharset) otherTile;
				}
			}

			if (neighborPos != null) {
				IBlockState newState = world.getBlockState(pos);
				EnumFacing selfFacing = player.getHorizontalFacing().getOpposite();

				if (neighborFacing.getAxis() == selfFacing.getAxis()) {
					selfFacing = selfFacing.rotateY();
				}

				world.setBlockState(neighborPos, world.getBlockState(neighborPos).withProperty(Properties.FACING4, selfFacing), 2);
				newState = newState.withProperty(Properties.FACING4, selfFacing);
				world.setBlockState(pos, newState, 2);

				chest.setNeighbor(neighbor, neighborFacing);
			}
		}
	}
}
