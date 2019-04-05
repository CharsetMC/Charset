/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.optics.projector;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.render.ArrowHighlightHandler;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.optics.projector.handlers.ProjectorHandlerBook;
import pl.asie.charset.module.optics.projector.handlers.ProjectorHandlerMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@CharsetModule(
		name = "optics.projector",
		description = "Projectors!",
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetProjector {
	@CharsetModule.Configuration
	public static Configuration config;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static boolean useLasers;
	public static Block blockProjector;
	public static Item itemProjector;
	private static List<IProjectorHandler<ItemStack>> handlerStack = new ArrayList<>();

	@Mod.EventHandler
	public void onLoadConfig(CharsetLoadConfigEvent event) {
		boolean forceLasers = ConfigUtils.getBoolean(config, "general", "forceLasers", false, "Forces usage of the laser API even if the optics.laser module is not present.", true);
		useLasers = forceLasers || ModCharset.isModuleLoaded("optics.laser");
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		blockProjector = new BlockProjector();
		itemProjector = new ItemBlockBase(blockProjector);
		itemProjector.setTranslationKey("charset.projector");

		handlerStack.add(new ProjectorHandlerBook());
		handlerStack.add(new ProjectorHandlerMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRegisterModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemProjector, 0, "charset:projector");
	}

	@Nullable
	public static IProjectorHandler<ItemStack> getHandler(ItemStack stack) {
		for (IProjectorHandler<ItemStack> handler : handlerStack) {
			if (handler.matches(stack)) {
				return handler;
			}
		}

		return null;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileProjector.class, "projector");
		packet.registerPacket(0x01, PacketRequestMapData.class);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileProjector.class, new TileProjectorRenderer());
		ArrowHighlightHandler.register(itemProjector);

		MinecraftForge.EVENT_BUS.register(new ProjectorRenderer());
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockProjector, "projector");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemProjector, "projector");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureMap(TextureStitchEvent.Pre event) {
		ProjectorModel.INSTANCE.template = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/projector"), event.getMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:projector", "normal"), ProjectorModel.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:projector", "inventory"), ProjectorModel.INSTANCE);
	}
}
