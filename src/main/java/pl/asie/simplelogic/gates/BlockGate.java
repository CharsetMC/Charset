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

package pl.asie.simplelogic.gates;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;

public class BlockGate extends BlockBase implements ITileEntityProvider {
	public static final UnlistedPropertyGeneric<PartGate> PROPERTY = new UnlistedPropertyGeneric<>("part", PartGate.class);

	public BlockGate() {
		super(Material.CIRCUITS);
		setHardness(0.0f);
		setSoundType(SoundType.WOOD);
		setOpaqueCube(false);
		setFullCube(false);
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		return () -> ImmutableList.copyOf(SimpleLogicGates.gateStacks);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess source, IBlockState state, BlockPos pos, EnumFacing facing) {
		TileEntity tile = source.getTileEntity(pos);
		if (tile instanceof PartGate) {
			if (((PartGate) tile).getOrientation().facing == facing.getOpposite()) {
				return BlockFaceShape.SOLID;
			}
		}
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity tile = source.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return ((PartGate) tile).getBox();
		} else {
			return Block.FULL_BLOCK_AABB;
		}
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return ((IExtendedBlockState) state).withProperty(PROPERTY, (PartGate) tile);
		} else {
			return state;
		}
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{PROPERTY});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}


	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return ((PartGate) tile).canConnectRedstone(side != null ? side.getOpposite() : null);
		} else {
			return false;
		}
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		TileEntity tile = blockAccess.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return ((PartGate) tile).getWeakSignal(side.getOpposite());
		} else {
			return 0;
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return ((PartGate) tile).onActivated(playerIn, hand, hitX, hitY, hitZ);
		} else {
			return false;
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof PartGate) {
			((PartGate) tile).onNeighborBlockChange(fromPos, blockIn);
		}
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return ((PartGate) tile).rotate(axis);
		} else {
			return false;
		}
	}

	@Override
	@Nullable
	public EnumFacing[] getValidRotations(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return new EnumFacing[] { ((PartGate) tile).getOrientation().facing, ((PartGate) tile).getOrientation().facing.getOpposite() };
		} else {
			return null;
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new PartGate();
	}

	// comparator support

	@Override
	public void onNeighborChange(IBlockAccess worldIn, BlockPos pos, BlockPos neighbor) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof PartGate) {
			PartGate gate = ((PartGate) tile);
			if (gate.logic.hasComparatorInputs()) {
				((PartGate) tile).onChanged();
				return;
			}
		}
	}

	@Override
	public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
		return true;
	}
}
