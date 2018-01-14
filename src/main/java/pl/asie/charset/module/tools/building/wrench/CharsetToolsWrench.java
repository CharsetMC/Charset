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

package pl.asie.charset.module.tools.building.wrench;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tools.building.ToolItemColor;
import pl.asie.charset.module.tools.building.wrench.ItemWrench;

@CharsetModule(
		name = "tools.building.wrench",
		description = "Rotating wrench. Soon to be merged into building.",
		dependencies = "tools.building",
		profile = ModuleProfile.STABLE
)
public class CharsetToolsWrench {
	public static ItemWrench wrench;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wrench = new ItemWrench();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(ToolItemColor.INSTANCE, wrench);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		/* CharsetToolsBuilding.registerRotationHandler(Blocks.CHEST, ((world, pos, state, axis) -> {
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				BlockPos pos2 = pos.offset(facing);
				IBlockState state2 = world.getState(pos2);
				if (state2.getBlock() == state.getBlock()) {
					EnumFacing newDir = state.getValue(BlockChest.FACING).getOpposite();
					world.setBlockState(pos, state.withProperty(BlockChest.FACING, newDir));
					world.setBlockState(pos2, state2.withProperty(BlockChest.FACING, newDir));
					return true;
				}
			}

			return state.getBlock().rotateBlock(world, pos, axis);
		})); */
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(wrench, 0, "charset:wrench");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), wrench, "wrench");
	}
}
