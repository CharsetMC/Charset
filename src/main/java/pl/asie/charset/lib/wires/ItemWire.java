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

package pl.asie.charset.lib.wires;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.wires.WireUtils;
import pl.asie.charset.wires.logic.PartWireProvider;

import java.util.List;

public class ItemWire extends ItemMultiPart {
	public ItemWire() {
		super();
		setHasSubtypes(true);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	public WireFactory getFactory(ItemStack stack) {
		return WireManager.REGISTRY.getObjectById(stack.getMetadata() >> 1);
	}

	public PartWire fromStack(ItemStack stack, EnumFacing facing) {
		WireFactory factory = getFactory(stack);
		if (factory != null) {
			PartWire wire = factory.createPart(stack);
			wire.location = isFreestanding(stack) ? WireFace.CENTER : WireFace.get(facing);
			return wire;
		} else {
			return null;
		}
	}

	@Override
	public boolean place(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		WireFactory factory = getFactory(stack);
		if (factory == null || (!isFreestanding(stack) && !factory.canPlace(world, pos.offset(side), WireFace.get(side.getOpposite())))) {
			return false;
		}

		return super.place(world, pos, side, hit, stack, player);
	}

	@Override
	public IMultipart createPart(World world, BlockPos blockPos, EnumFacing facing, Vec3d vec3, ItemStack stack, EntityPlayer player) {
		return fromStack(stack, facing);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (ResourceLocation location : WireManager.REGISTRY.getKeys()) {
			int id = WireManager.REGISTRY.getId(location);
			subItems.add(new ItemStack(itemIn, 1, id * 2));
			subItems.add(new ItemStack(itemIn, 1, id * 2 + 1));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		PartWire wire = fromStack(stack, EnumFacing.DOWN);
		if (wire != null) {
			return wire.getDisplayName();
		} else {
			return "wire.null";
		}
	}

	public static boolean isFreestanding(ItemStack stack) {
		return (stack.getItemDamage() & 1) == 1;
	}
}
