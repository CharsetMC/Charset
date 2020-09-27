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

package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;

public class BlockCompressionCrafter extends BlockBase implements ITileEntityProvider {
	protected static final PropertyInteger OFFSET_X = PropertyInteger.create("offset_x", 0, 3);
	protected static final PropertyInteger OFFSET_Y = PropertyInteger.create("offset_y", 0, 7);
	protected static final PropertyInteger OFFSET_Z = PropertyInteger.create("offset_z", 0, 3);

	public BlockCompressionCrafter() {
		super(Material.ROCK);
		setSoundType(SoundType.STONE);
		setHardness(0.5F);
		setOpaqueCube(false);
		setTranslationKey("charset.compression_crafter");
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return state.getValue(Properties.FACING).getAxis() == face.getAxis();
	}

	private int modelOffset(IBlockAccess access, BlockPos pos, EnumFacing facing, EnumFacing.Axis axis, boolean flip) {
		IBlockState stateLeft, stateRight;
		if (flip) {
			stateLeft = access.getBlockState(pos.offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis)));
			stateRight = access.getBlockState(pos.offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, axis)));
		} else {
			stateLeft = access.getBlockState(pos.offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, axis)));
			stateRight = access.getBlockState(pos.offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis)));
		}
		return ((stateLeft.getBlock() instanceof BlockCompressionCrafter && stateLeft.getValue(Properties.FACING) == facing) ? 2 : 0)
				| ((stateRight.getBlock() instanceof BlockCompressionCrafter && stateRight.getValue(Properties.FACING) == facing) ? 1 : 0);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		EnumFacing facing = state.getValue(Properties.FACING);
		EnumFacing.Axis axisY, axisX, axisZ;
		int yOffset = 0;
		boolean flip = false;
		// determine X and Z axis
		//axisY = facing.getAxis();
		/* switch (axisY) {
			case Y:
			default:
				axisX = EnumFacing.Axis.X;
				axisZ = EnumFacing.Axis.Z;
				break;
			case X:
				axisX = EnumFacing.Axis.Y;
				axisZ = EnumFacing.Axis.Z;
				flip = facing == EnumFacing.WEST;
				break;
			case Z:
				axisX = EnumFacing.Axis.X;
				axisZ = EnumFacing.Axis.Y;
				flip = facing == EnumFacing.SOUTH;
				break;
		} */
		switch (facing.getAxis()) {
			case Y:
			default:
				if (modelOffset(worldIn, pos, facing, EnumFacing.Axis.X, flip) > 0) {
					axisY = EnumFacing.Axis.X;
				} else {
					axisY = EnumFacing.Axis.Z;
					yOffset = 4;
				}
				axisX = EnumFacing.Axis.Z;
				axisZ = EnumFacing.Axis.X;
				break;
			case X:
				if (modelOffset(worldIn, pos, facing, EnumFacing.Axis.Z, flip) > 0) {
					axisX = EnumFacing.Axis.Z;
					axisZ = EnumFacing.Axis.X;
				} else {
					axisX = EnumFacing.Axis.Y;
					axisZ = EnumFacing.Axis.Y;
					yOffset = 4;
				}
				axisY = EnumFacing.Axis.Z;
				break;
			case Z:
				if (modelOffset(worldIn, pos, facing, EnumFacing.Axis.X, flip) > 0) {
					axisX = EnumFacing.Axis.Z;
					axisZ = EnumFacing.Axis.X;
				} else {
					axisX = EnumFacing.Axis.Y;
					axisZ = EnumFacing.Axis.Y;
					yOffset = 4;
				}
				axisY = EnumFacing.Axis.X;
				break;
		}
		return state
				.withProperty(OFFSET_X, modelOffset(worldIn, pos, facing, axisX, flip))
				.withProperty(OFFSET_Y, modelOffset(worldIn, pos, facing, axisY, flip) + yOffset)
				.withProperty(OFFSET_Z, modelOffset(worldIn, pos, facing, axisZ, flip));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING, OFFSET_X, OFFSET_Y, OFFSET_Z);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING, EnumFacing.byIndex(meta & 7));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING).ordinal();
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(Properties.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileCompressionCrafter();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCompressionCrafter) {
			((TileCompressionCrafter) tile).onNeighborChange(state);
		}
	}
}
