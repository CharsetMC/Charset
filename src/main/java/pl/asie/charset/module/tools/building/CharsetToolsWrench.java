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

package pl.asie.charset.module.tools.building;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tools.building.wrench.ItemWrench;

@CharsetModule(
		name = "tools.wrench",
		description = "Rotating wrench. Soon to be merged into building.",
		profile = ModuleProfile.STABLE
)
public class CharsetToolsWrench {
	public static ItemWrench wrench;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wrench = new ItemWrench();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		/* CharsetToolsBuilding.registerRotationHandler(Blocks.CHEST, ((world, pos, state, axis) -> {
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				BlockPos pos2 = pos.offset(facing);
				IBlockState state2 = world.getBlockState(pos2);
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
