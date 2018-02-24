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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

public class ItemBlockAxle extends ItemBlockBase {
	public ItemBlockAxle(Block block) {
		super(block);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		Block block = worldIn.getBlockState(pos).getBlock();

		if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
			side = EnumFacing.UP;
		} else if (!block.isReplaceable(worldIn, pos)) {
			pos = pos.offset(side);
		}

		IBlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if (state.getBlock() instanceof BlockAxle) {
			TileEntity tile = worldIn.getTileEntity(pos.offset(side.getOpposite()));
			if (tile instanceof TileAxle) {
				EnumFacing.Axis axis = state.getValue(Properties.AXIS);
				ItemMaterial other = ((TileAxle) tile).getMaterial();
				if (axis == side.getAxis() && other == ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material", "plank")) {
					return true;
				} else {
					return false;
				}
			}
		}

		return worldIn.mayPlace(this.block, pos, false, side, (Entity)null);
	}
}
