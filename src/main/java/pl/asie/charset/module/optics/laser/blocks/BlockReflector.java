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

package pl.asie.charset.module.optics.laser.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.module.optics.laser.CharsetLaser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockReflector extends BlockBase implements ITileEntityProvider {
	// DOWN: XZ, NORTH->WEST
	// UP: XZ, NORTH->EAST
	// NORTH: XY, DOWN->EAST
	// SOUTH: XY, DOWN->WEST
	// WEST: YZ, DOWN->NORTH
	// EAST: YZ, DOWN->SOUTH
	public static final PropertyDirection ROTATION = PropertyDirection.create("rotation");
	public static final PropertyBool SPLITTER = PropertyBool.create("splitter");
	public static final AxisAlignedBB BOX = new AxisAlignedBB(0.25f,0.25f,0.25f,0.75f,0.75f,0.75f);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOX;
	}

	public static @Nonnull EnumFacing getTargetFacing(@Nonnull EnumFacing facing, @Nonnull EnumFacing rotation) {
		if (facing.getAxis() == rotation.getAxis())
			return facing.getOpposite();

		switch (rotation.getAxis()) {
			case Y:
				if (facing.getAxis() == EnumFacing.Axis.Z)
					rotation = rotation.getOpposite();
				break;
			case X:
			case Z:
				if (facing.getAxis() == EnumFacing.Axis.Y)
					rotation = rotation.getOpposite();
				break;
		}

		switch (rotation) {
			case DOWN:
				return facing.rotateYCCW();
			case UP:
				return facing.rotateY();
			case NORTH:
				return facing.rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z);
			case SOUTH:
				return facing.rotateAround(EnumFacing.Axis.Z);
			case WEST:
				return facing.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.X);
			case EAST:
				return facing.rotateAround(EnumFacing.Axis.X);
		}

		throw new RuntimeException("Should never reach here!");
	}

	public BlockReflector() {
		super(Material.GLASS);
		setHardness(0.4F);
		setSoundType(SoundType.GLASS);
		setOpaqueCube(false);
		setFullCube(false);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ROTATION, SPLITTER, CharsetLaser.LASER_COLOR);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ROTATION, EnumFacing.byIndex(meta & 7)).withProperty(SPLITTER, (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ROTATION).ordinal() | (state.getValue(SPLITTER) ? 8 : 0);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> itemList) {
		for (int i = 0; i <= 16; i++) {
			if ((i & 7) > 0) {
				itemList.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState state = world.getBlockState(pos);
		EnumFacing facing = state.getValue(ROTATION);
		EnumFacing newFacing = facing;

		if (facing.getAxis() == axis.getAxis()) {
			newFacing = facing.getOpposite();
		} else {
			newFacing = axis;
		}

		if (facing != newFacing) {
			world.setBlockState(pos, state.withProperty(ROTATION, newFacing));
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileReflector) {
				((TileReflector) tile).updateRotations();
			}
		}
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileReflector();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileReflector) {
			return state.withProperty(CharsetLaser.LASER_COLOR, ((TileReflector) tile).getColor());
		} else {
			return state;
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand)
				.withProperty(SPLITTER, (meta & 8) != 0);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		EnumFacing facing = state.getValue(ROTATION);
		int count = RotationUtils.getClockwiseRotationCount(rot);
		for (int i = 0; i < count; i++) {
			switch (facing) {
				case DOWN:
				case UP:
					facing = facing.getOpposite();
					break;
				case NORTH:
				case SOUTH:
				case WEST:
				case EAST:
					facing = facing.rotateY();
					break;
			}
		}
		return state.withProperty(ROTATION, facing);
	}
}
