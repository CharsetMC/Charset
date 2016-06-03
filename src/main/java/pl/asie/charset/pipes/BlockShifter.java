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

package pl.asie.charset.pipes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.refs.Properties;

public class BlockShifter extends BlockContainer {
	public static final PropertyBool EXTRACT = PropertyBool.create("extract");
	public static final PropertyInteger STRENGTH = PropertyInteger.create("strength", 0, 2);

	public BlockShifter() {
		super(Material.IRON);
		setUnlocalizedName("charset.shifter");
		setDefaultState(this.blockState.getBaseState().withProperty(Properties.FACING, EnumFacing.NORTH));
		setHardness(1.5F);
	}

	public boolean isValidFacing(World world, BlockPos pos, EnumFacing facing) {
		return PipeUtils.getPipe(world, pos.offset(facing), facing.getOpposite()) != null;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileShifter) {
			int[] shiftedCoordinates = new int[6];
			TileShifter shifter = (TileShifter) tileEntity;

			for (EnumFacing side : EnumFacing.VALUES) {
				EnumFacing newSide = side;

				switch (shifter.getDirection(state)) {
					case DOWN:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.X);
						break;
					case NORTH:
						newSide = side.rotateAround(EnumFacing.Axis.X);
						break;
					case EAST:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.Y);
						break;
					case SOUTH:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y);
						break;
					case WEST:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y);
						break;
				}

				shiftedCoordinates[side.ordinal()] = newSide.ordinal();
			}

			return state
					.withProperty(EXTRACT, shifter.getMode() == IShifter.Mode.Extract)
					.withProperty(STRENGTH, /* shifter.getRedstoneLevel() >= 8 ? 2 : */ (shifter.getRedstoneLevel() > 0 ? 2 : 0))
					.withProperty(Properties.DOWN, shifter.getFilters()[shiftedCoordinates[0]] != null)
					.withProperty(Properties.UP, shifter.getFilters()[shiftedCoordinates[1]] != null)
					.withProperty(Properties.NORTH, shifter.getFilters()[shiftedCoordinates[2]] != null)
					.withProperty(Properties.SOUTH, shifter.getFilters()[shiftedCoordinates[3]] != null)
					.withProperty(Properties.WEST, shifter.getFilters()[shiftedCoordinates[4]] != null)
					.withProperty(Properties.EAST, shifter.getFilters()[shiftedCoordinates[5]] != null);
		} else {
			return state;
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (side == null) {
			return false;
		}

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileShifter) {
			TileShifter shifter = (TileShifter) tileEntity;

			if (side == shifter.getDirection()) {
				return false;
			}

			if (shifter.getFilters()[side.ordinal()] != null) {
				if (!world.isRemote) {
					shifter.setFilter(side.ordinal(), null);
				}
				return true;
			} else if (heldItem != null) {
				if (!world.isRemote) {
					ItemStack filter = heldItem.copy();
					filter.stackSize = 1;
					shifter.setFilter(side.ordinal(), filter);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		PartPipe partForward = PipeUtils.getPipe(world, pos.offset(facing), facing.getOpposite());
		PartPipe partBackward = PipeUtils.getPipe(world, pos.offset(facing.getOpposite()), facing);

		if (partBackward instanceof PartPipe) {
			return this.getStateFromMeta(facing.getOpposite().ordinal());
		} else if (partForward instanceof PartPipe) {
			return this.getStateFromMeta(facing.ordinal());
		} else {
			for (EnumFacing direction : EnumFacing.VALUES) {
				if (isValidFacing(world, pos, direction)) {
					return this.getStateFromMeta(direction.ordinal());
				}
			}

			return this.getStateFromMeta(facing.ordinal());
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this,
				STRENGTH, EXTRACT,
				Properties.FACING,
				Properties.DOWN,
				Properties.UP,
				Properties.NORTH,
				Properties.SOUTH,
				Properties.WEST,
				Properties.EAST
		);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(Properties.FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING).ordinal();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileShifter();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileShifter) {
			((TileShifter) tile).updateRedstoneLevel();
		}
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState state = world.getBlockState(pos);

		int m = state.getValue(Properties.FACING).ordinal();
		for (int i = 1; i < 6; i++) {
			EnumFacing f = EnumFacing.getFront((m + i) % 6);

			if (isValidFacing(world, pos, f)) {
				world.setBlockState(pos, state.withProperty(Properties.FACING, f), 3);
				return true;
			}
		}

		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
