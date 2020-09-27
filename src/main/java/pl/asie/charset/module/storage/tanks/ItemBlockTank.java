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

package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.utils.ColorUtils;

public class ItemBlockTank extends ItemBlockBase {
	public ItemBlockTank(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return 0;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound()) {
			int color = stack.getTagCompound().getInteger("color");
			if (color >= 0 && color < 16) {
				return I18n.translateToLocalFormatted("tile.charset.tank.colored.name", I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", EnumDyeColor.byMetadata(stack.getTagCompound().getInteger("color")))));
			} else if (color == 16) {
				return I18n.translateToLocal("tile.charset.tank.creative.name");
			} else {
				return I18n.translateToLocal("tile.charset.tank.name");
			}
		} else {
			return I18n.translateToLocal("tile.charset.tank.name");
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return "tile.charset.tank.name";
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (TileTank.checkPlacementConflict(worldIn.getTileEntity(pos.down()), worldIn.getTileEntity(pos.up()), (stack.getTagCompound().getInteger("color") + 1) % BlockTank.VARIANTS)) {
			return false;
		} else {
			return super.placeBlockAt(stack, player, worldIn, pos, side, hitX, hitY, hitZ, newState);
		}
	}
}
