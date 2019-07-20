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

package pl.asie.charset.module.tweak.coloredSlimeBlocks;

import net.minecraft.block.BlockSlime;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.IPatchCanPushListener;

public class BlockSlimeColored extends BlockSlime implements IPatchCanPushListener {
	public BlockSlimeColored() {
		super();
		setSoundType(SoundType.SLIME);
		// TODO: set correct map color
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.COLOR);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.COLOR, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.COLOR);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(Properties.COLOR, meta & 15);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(Properties.COLOR);
	}

	@Override
	public boolean isStickyBlock(IBlockState state) {
		return true;
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		for (int i = 0; i < 16; i++) {
			items.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public boolean charsetCanPushByPiston(IBlockState blockStateIn, World worldIn, BlockPos pos, EnumFacing facing, boolean destroyBlocks, EnumFacing p_185646_5_) {
		IBlockState source = worldIn.getBlockState(pos.offset(p_185646_5_.getOpposite()));
		if (source.getBlock() instanceof BlockSlimeColored && source.getValue(Properties.COLOR) != blockStateIn.getValue(Properties.COLOR)) {
			return false;
		}
		return true;
	}
}
