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

package pl.asie.charset.lib.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.utils.UtilProxyClient;

import javax.annotation.Nullable;

public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);
		setCreativeTab(ModCharset.CREATIVE_TAB);
	}

	public final ISubItemProvider getSubItemProvider() {
		return block instanceof BlockBase ? ((BlockBase) block).getSubItemProvider() : null;
	}

	@Override
	public int getMetadata(int damage) {
		int stateCount = block.getBlockState().getValidStates().size();
		if (stateCount > 1) {
			return damage;
		} else {
			return 0;
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!world.setBlockState(pos, newState, 11)) {
			return false;
		}

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this.block) {
			setTileEntityNBT(world, player, pos, stack);
			if (this.block instanceof BlockBase) {
				((BlockBase) this.block).onBlockPlacedBy(world, pos, state, player, stack, side, hitX, hitY, hitZ);
			} else {
				this.block.onBlockPlacedBy(world, pos, state, player, stack);
			}

			if (player instanceof EntityPlayerMP)
				CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nullable
	public FontRenderer getFontRenderer(ItemStack stack) {
		return UtilProxyClient.FONT_RENDERER_FANCY;
	}
}
