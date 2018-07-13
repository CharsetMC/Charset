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
import net.minecraft.entity.player.EntityPlayer;
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
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;
import java.util.List;

public class BlockHandCrank extends BlockBase implements ITileEntityProvider {
	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

	static {
		BOXES[0] = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		for (int i = 1; i < 6; i++) {
			BOXES[i] = RotationUtils.rotateFace(BOXES[0], EnumFacing.getFront(i));
		}
	}

	public BlockHandCrank() {
		super(Material.WOOD);
		setHardness(0.5F);
		setHarvestLevel("axe", 0);
		setUnlocalizedName("charset.hand_crank");
		setOpaqueCube(false);
		setFullCube(false);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileHandCrank) {
			((TileHandCrank) tile).onActivated(playerIn);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		TileEntity tile = worldIn.getTileEntity(pos.offset(side.getOpposite()));
		return tile != null && tile.hasCapability(Capabilities.MECHANICAL_CONSUMER, side);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOXES[state.getValue(Properties.FACING).ordinal()];
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(Properties.FACING, facing);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING, EnumFacing.getFront(meta));
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileHandCrank();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

}
