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

package pl.asie.charset.module.tools.wrench;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

import java.util.HashMap;
import java.util.Map;

@CharsetModule(
		name = "tools.wrench",
		description = "Simple block-rotating wrench",
		profile = ModuleProfile.STABLE
)
public class CharsetToolsWrench {
	private static Map<Block, ICustomRotateBlock> customRotationHandlers = new HashMap<>();

	public static ItemWrench wrench;

	public static ICustomRotateBlock getRotationHandler(Block block) {
		return customRotationHandlers.get(block);
	}

	public static void registerRotationHandler(Block block, ICustomRotateBlock rotateBlock) {
		if (customRotationHandlers.containsKey(block)) {
			throw new RuntimeException("Duplicate rotation handlers for " + block.getRegistryName() + "! " + rotateBlock.getClass().getName() + ", " + customRotationHandlers.get(block).getClass().getName());
		}

		customRotationHandlers.put(block, rotateBlock);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wrench = new ItemWrench();
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
