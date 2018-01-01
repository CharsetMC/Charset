/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tools.building;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
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

import java.util.HashMap;
import java.util.Map;

@CharsetModule(
		name = "tools.building",
		description = "Building tools: chisel.",
		profile = ModuleProfile.EXPERIMENTAL // TODO: When STABLE, merge tools.wrench
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(ToolItemColor.INSTANCE, chisel);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.CHISEL, Side.CLIENT, (r) -> new GuiChisel((ContainerChisel) r.getContainer()));
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
