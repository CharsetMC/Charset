/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
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
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.item.SubItemSetHelper;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import java.util.List;

public class BlockChestCharset extends BlockBase implements ITileEntityProvider {
	protected static final IUnlistedProperty<ItemMaterial> MATERIAL_PROP = new UnlistedPropertyGeneric<>("material", ItemMaterial.class);
	protected static final AxisAlignedBB[] FACING_BOXES = new AxisAlignedBB[6];

	static {
		FACING_BOXES[0] =  new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375);
		FACING_BOXES[1] = FACING_BOXES[0];
		FACING_BOXES[2] = new AxisAlignedBB(0.0625, 0.0, 0.0, 0.9375, 0.875, 0.9375);
		FACING_BOXES[3] = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 1.0);
		FACING_BOXES[4] = new AxisAlignedBB(0.0, 0.0, 0.0625, 0.9375, 0.875, 0.9375);
		FACING_BOXES[5] = new AxisAlignedBB(0.0625, 0.0, 0.0625, 1.0, 0.875, 0.9375);
	}

	public BlockChestCharset() {
		super(Material.WOOD);
		setFullCube(false);
		setOpaqueCube(false);
		setHardness(2.5F);
		setSoundType(SoundType.WOOD);
		setTranslationKey("chest");
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		 return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetStorageChests.itemChest) {
			@Override
			protected int compareSets(List<ItemStack> first, List<ItemStack> second) {
				return SubItemSetHelper.wrapLists(first, second, SubItemSetHelper.extractMaterial("wood", SubItemSetHelper::sortByItem));
			}
		});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasCustomBreakingProgress(IBlockState state) {
		return true;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityChestCharset) {
				return ((IExtendedBlockState) state).withProperty(MATERIAL_PROP, ((TileEntityChestCharset) tile).material.getMaterial());
			}
		}

		return state;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		int id = 0;
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityChestCharset) {
			if (((TileEntityChestCharset) tile).hasNeighbor()) {
				id = ((TileEntityChestCharset) tile).getNeighborFace().ordinal();
			}
		}

		return FACING_BOXES[id];
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getStateFromMeta(meta).withProperty(
				Properties.FACING4,
				placer.getHorizontalFacing().getOpposite()
		);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityChestCharset) {
			((TileEntityChestCharset) tile).getNeighbor();
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world != null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityChestCharset) {
				return ((TileEntityChestCharset) tile).activate(player, side, hand);
			}
		}

		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[] { Properties.FACING4 }, new IUnlistedProperty[] {MATERIAL_PROP});
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING4).ordinal() - 2;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING4, EnumFacing.byIndex((meta & 3) + 2));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityChestCharset();
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState state = world.getBlockState(pos);
		EnumFacing facing = state.getValue(Properties.FACING4);
		EnumFacing newFacing = axis == EnumFacing.DOWN ? facing.rotateYCCW() : facing.rotateY();

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileEntityChestCharset)) {
			return false;
		} else if (((TileEntityChestCharset) tile).hasNeighbor()) {
			BlockPos otherPos = ((TileEntityChestCharset) tile).getNeighbor().getPos();
			IBlockState otherState = world.getBlockState(otherPos);
			EnumFacing otherFacing = otherState.getValue(Properties.FACING4);
			if (facing != otherFacing) {
				return false;
			}

			newFacing = facing.rotateY().rotateY();
			world.setBlockState(otherPos, otherState.withProperty(Properties.FACING4, newFacing));
		}

		world.setBlockState(pos, state.withProperty(Properties.FACING4, newFacing));
		return true;
	}
}
