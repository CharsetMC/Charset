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

package pl.asie.charset.wires;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.wires.logic.PartWireSignalBase;
import pl.asie.charset.wires.logic.PartWireProvider;

public class ItemWireOld extends ItemMultiPart {
	public ItemWireOld() {
		super();
		setHasSubtypes(true);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	public boolean place(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		if (!isFreestanding(stack) && !WireUtils.canPlaceWire(world, pos.offset(side), side.getOpposite())) {
			return false;
		}

		return super.place(world, pos, side, hit, stack, player);
	}

	@Override
	public IMultipart createPart(World world, BlockPos blockPos, EnumFacing facing, Vec3d vec3, ItemStack stack, EntityPlayer player) {
		/* PartWireSignalBase part = PartWireProvider.createPart(stack.getItemDamage() >> 1);
		part.location = isFreestanding(stack) ? WireFace.CENTER : WireFace.get(facing);
		return part; */
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < 18 * 2; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return "wire.null";
	}

	public static boolean isFreestanding(ItemStack stack) {
		return (stack.getItemDamage() & 1) == 1;
	}
}
