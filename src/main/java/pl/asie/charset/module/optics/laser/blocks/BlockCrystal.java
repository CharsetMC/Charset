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

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.module.optics.laser.CharsetLaser;
import pl.asie.charset.api.laser.LaserColor;

import javax.annotation.Nullable;

public class BlockCrystal extends BlockBase implements ITileEntityProvider {
	public static final PropertyBool OPAQUE = PropertyBool.create("opaque");

	public BlockCrystal() {
		super(Material.GLASS);
		setHardness(0.4F);
		setSoundType(SoundType.GLASS);
		setDefaultState(getBlockState().getBaseState().withProperty(OPAQUE, false));
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return state.getValue(OPAQUE);
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return !worldIn.getBlockState(pos).getValue(OPAQUE);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return state.getValue(OPAQUE);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, CharsetLaser.LASER_COLOR, OPAQUE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(OPAQUE, (meta & 8) != 0).withProperty(CharsetLaser.LASER_COLOR, LaserColor.VALUES[meta & 7]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(CharsetLaser.LASER_COLOR).ordinal() | (state.getValue(OPAQUE) ? 8 : 0);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> itemList) {
		for (int i = 1; i <= 7; i++) {
			itemList.add(new ItemStack(this, 1, i));
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileCrystal();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getAmbientOcclusionLightValue(IBlockState state) {
		return 1.0f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return state.getValue(OPAQUE) ? (layer == BlockRenderLayer.SOLID) : (layer == BlockRenderLayer.TRANSLUCENT);
	}
}
