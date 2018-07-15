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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.item.SubItemSetHelper;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;
import java.util.List;

public class BlockAxle extends BlockBase implements ITileEntityProvider {
	private static final IUnlistedProperty<ItemMaterial> MATERIAL = new UnlistedPropertyGeneric<>("material", ItemMaterial.class);
	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[] {
			new AxisAlignedBB(0/16f, 6/16f, 6/16f, 16/16f, 10/16f, 10/16f),
			new AxisAlignedBB(6/16f, 0/16f, 6/16f, 10/16f, 16/16f, 10/16f),
			new AxisAlignedBB(6/16f, 6/16f, 0/16f, 10/16f, 10/16f, 16/16f)
	};

	public BlockAxle() {
		super(Material.WOOD);
		setHardness(1.0F);
		setHarvestLevel("axe", 0);
		setTranslationKey("charset.axle");
		setOpaqueCube(false);
		setFullCube(false);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileAxle) {
			((TileAxle) tile).onNeighborChanged(fromPos);
		}
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetPowerMechanical.itemAxle) {
			@Override
			protected int compareSets(List<ItemStack> first, List<ItemStack> second) {
				return SubItemSetHelper.wrapLists(first, second, SubItemSetHelper.extractMaterial("material", SubItemSetHelper::sortByItem));
			}
		});
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOXES[state.getValue(Properties.AXIS).ordinal()];
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		world.setBlockState(pos, world.getBlockState(pos).cycleProperty(Properties.AXIS), 3);
		return true;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(Properties.AXIS, facing.getAxis());
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		EnumFacing.Axis axis = state.getValue(Properties.AXIS);
		if (face.getAxis() == axis) {
			return BlockFaceShape.CENTER;
		} else if (axis == EnumFacing.Axis.Y) {
			return BlockFaceShape.MIDDLE_POLE;
		} else {
			return BlockFaceShape.UNDEFINED;
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing.getAxis() == state.getValue(Properties.AXIS)) {
				IMechanicalPowerConsumer consumer = CapabilityHelper.get(worldIn, pos.offset(facing), Capabilities.MECHANICAL_CONSUMER, facing.getOpposite(),
						false, true, false);
				if (consumer != null) {
					consumer.setForce(0, 0);
				}
			}
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this,
				new IProperty[] { Properties.AXIS },
				new IUnlistedProperty[] { MATERIAL });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.AXIS).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.AXIS, EnumFacing.Axis.values()[meta]);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileAxle();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
