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

package pl.asie.charset.storage.backpack;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.BlockBase;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.refs.Properties;
import pl.asie.charset.storage.ModCharsetStorage;

public class BlockBackpack extends BlockBase implements ITileEntityProvider {
	public static class Color implements IBlockColor {
		@Override
		@SideOnly(Side.CLIENT)
		public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
			if (worldIn != null && pos != null) {
				TileEntity tile = worldIn.getTileEntity(pos);
				if (tile instanceof TileBackpack) {
					return ((TileBackpack) tile).getColor();
				}
			}

			return DEFAULT_COLOR;
		}
	}

	public static final int DEFAULT_COLOR = 0x805038;
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.1875f, 0.0f, 0.1875f, 0.8125f, 0.75f, 0.8125f);

	public BlockBackpack() {
		super(Material.CLOTH);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		setUnlocalizedName("charset.backpack");
		setHardness(0.8f);
		setSoundType(SoundType.CLOTH);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB;
	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		if (player.isSneaking()) {
			return -1.0f;
		} else {
			return super.getPlayerRelativeBlockHardness(state, player, world, pos);
		}
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (player.isSneaking() && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBackpack) {
				ItemStack stack = ((TileBackpack) tile).writeToItemStack();

				world.removeTileEntity(pos);
				world.setBlockToAir(pos);

				player.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileBackpack) {
			player.openGui(ModCharsetStorage.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (world != null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBackpack) {
				return ((TileBackpack) tile).writeToItemStack();
			}
		}

		return new ItemStack(this);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tile = worldIn.getTileEntity(pos);

		if (tile instanceof IInventory) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tile);
		}

		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(Properties.FACING4, placer.getHorizontalFacing());
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING4);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING4).ordinal() - 2;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING4, EnumFacing.getFront((meta & 3) + 2));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileBackpack();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
