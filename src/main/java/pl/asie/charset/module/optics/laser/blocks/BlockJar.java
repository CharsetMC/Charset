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

package pl.asie.charset.module.optics.laser.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.module.optics.laser.CharsetLaser;

import javax.annotation.Nullable;

public class BlockJar extends BlockBase implements ITileEntityProvider {
	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

	static {
		BOXES[1] = new AxisAlignedBB(6/16f, 0, 6/16f, 10/16f, 10/16f, 10/16f);
		for (int i = 1; i < 6; i++) {
			BOXES[i ^ 1] = RotationUtils.rotateFace(BOXES[1], EnumFacing.getFront(i));
		}
	}

	public BlockJar() {
		super(Material.GLASS);
		setOpaqueCube(false);
		setFullCube(false);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) {
			return true;
		}

		// TODO: This is temporary until a way to light jars up is implemented.
		ItemStack stack = playerIn.getHeldItem(hand);
		if (!stack.isEmpty() && stack.getItem() instanceof ItemFlintAndSteel) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileJar) {
				((TileJar) tile).setColor(LaserColor.WHITE);
				return true;
			}
		}

		return false;
	}


	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOXES[state.getValue(Properties.FACING).ordinal()];
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	protected boolean canPlaceAt(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		BlockPos destPos = pos.offset(facing.getOpposite());
		IBlockState destState = world.getBlockState(destPos);
		if (destState.getBlock() instanceof BlockPistonExtension) {
			return destState.getValue(BlockPistonExtension.FACING) == facing;
		} else if (destState.getBlock() instanceof BlockPistonBase) {
			return true;
		} else {
			return destState.isSideSolid(world, destPos, facing);
		}
	}

	protected boolean dropIfNotBacked(World world, BlockPos pos, IBlockState state) {
		if (state.getBlock() == this) {
			EnumFacing facing = state.getValue(Properties.FACING);
			if (!canPlaceAt(world, pos, facing)) {
				dropBlockAsItem(world, pos, state, 0);
				world.setBlockToAir(pos);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		return canPlaceAt(world, pos, side);
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		dropIfNotBacked(worldIn, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		dropIfNotBacked(worldIn, pos, state);
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		if (super.rotateBlock(world, pos, axis)) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileJar) {
				((TileJar) tile).updateRotations();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING, CharsetLaser.LASER_COLOR);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING, EnumFacing.getFront(meta & 7));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING).ordinal();
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileJar();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> itemList) {
		for (int i = 0; i <= 7; i++) {
			itemList.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileJar) {
			return state.withProperty(CharsetLaser.LASER_COLOR, ((TileJar) tile).getColor());
		} else {
			return state;
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(Properties.FACING, facing);
	}
}
