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

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tools.building.chisel.*;
import pl.asie.charset.module.tools.building.trowel.ItemTrowel;
import pl.asie.charset.module.tools.building.wrench.ICustomRotateBlock;

import java.util.HashMap;
import java.util.Map;

@CharsetModule(
		name = "tools.building",
		description = "Building tools: chisel.",
		profile = ModuleProfile.UNSTABLE // TODO: When STABLE, merge tools.wrench
)
public class CharsetToolsBuilding {
	private static Map<Block, ICustomRotateBlock> customRotationHandlers = new HashMap<>();

	public static ItemChisel chisel;
	public static ItemTrowel trowel;

	@CharsetModule.PacketRegistry("toolsBldng")
	public static PacketRegistry packet;

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
		chisel = new ItemChisel();
		// trowel = new ItemTrowel();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketSetBlockMask.class);
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.CHISEL, Side.SERVER, (r) -> new ContainerChisel(r.player));
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ToolItemColor.INSTANCE, chisel);
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.CHISEL, Side.CLIENT, (r) -> new GuiChisel(new ContainerChisel(r.player)));
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(chisel,0, "charset:chisel");
		// RegistryUtils.registerModel(trowel,0, "charset:trowel");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void bakeModels(ModelBakeEvent event) {
		ModelResourceLocation location = new ModelResourceLocation("charset:chisel", "inventory");
		IBakedModel model = event.getModelRegistry().getObject(location);
		if (model != null) {
			event.getModelRegistry().putObject(location, new ChiselBakedModel(model));
		}
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), chisel, "chisel");
		// RegistryUtils.register(event.getRegistry(), trowel, "trowel");
	}
}
