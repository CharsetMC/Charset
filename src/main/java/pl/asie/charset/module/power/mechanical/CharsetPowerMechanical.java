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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.capability.mechanical.DefaultMechanicalPowerConsumer;
import pl.asie.charset.lib.capability.mechanical.DefaultMechanicalPowerProducer;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerProducer;
import pl.asie.charset.module.power.mechanical.render.*;

@CharsetModule(
		name = "power.mechanical",
		description = "Mechanical power system",
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetPowerMechanical {
	public static BlockAxle blockAxle;
	public static BlockCreativeGenerator blockCreativeGenerator;
	public static BlockGearbox blockGearbox;
	public static BlockHandCrank blockHandCrank;
	public static BlockSocket blockSocket;
	public static ItemBlock itemAxle, itemCreativeGenerator, itemGearbox, itemHandCrank, itemSocket;

	private static final int[] GEAR_VALUES = new int[] { 1, 2, 3, 5 };
	private static final String[] GEAR_TYPES = new String[] { "Wood", "Stone", "Iron", "Gold" };
	private static ItemGear[] GEAR_ITEMS;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		blockAxle = new BlockAxle();
		blockCreativeGenerator = new BlockCreativeGenerator();
		blockGearbox = new BlockGearbox();
		blockHandCrank = new BlockHandCrank();
		blockSocket = new BlockSocket();

		itemAxle = new ItemBlockAxle(blockAxle);
		itemCreativeGenerator = new ItemBlockBase(blockCreativeGenerator);
		itemGearbox = new ItemBlockGearbox(blockGearbox);
		itemHandCrank = new ItemBlockBase(blockHandCrank);
		itemSocket = new ItemBlockBase(blockSocket);

		GEAR_ITEMS = new ItemGear[GEAR_VALUES.length];
		for (int i = 0; i < GEAR_VALUES.length; i++) {
			GEAR_ITEMS[i] = new ItemGear(GEAR_VALUES[i]);
			GEAR_ITEMS[i].setTranslationKey("charset.gear." + GEAR_VALUES[i]);
		}
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		RegistryUtils.register(TileAxle.class, "axle");
		RegistryUtils.register(TileCreativeGenerator.class, "creative_generator_mechanical");
		RegistryUtils.register(TileGearbox.class, "gearbox");
		RegistryUtils.register(TileHandCrank.class, "hand_crank");
		RegistryUtils.register(TileSocket.class, "socket_mechanical");

		for (int i = 0; i < GEAR_VALUES.length; i++) {
			OreDictionary.registerOre("gear" + GEAR_TYPES[i], GEAR_ITEMS[i]);
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPreInitClient(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(TileAxleRenderer.INSTANCE);
		MinecraftForge.EVENT_BUS.register(TileGearboxRenderer.INSTANCE);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onInitClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileAxle.class, TileAxleRenderer.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileGearbox.class, TileGearboxRenderer.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileHandCrank.class, TileHandCrankRenderer.INSTANCE);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegistry(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemAxle, 0, "charset:axle");
		RegistryUtils.registerModel(itemCreativeGenerator, 0, "charset:creative_generator_mechanical");
		RegistryUtils.registerModel(itemGearbox, 0, "charset:gearbox");
		RegistryUtils.registerModel(itemHandCrank, 0, "charset:hand_crank_mechanical");
		RegistryUtils.registerModel(itemSocket, 0, "charset:socket_mechanical");

		for (int i = 0; i < GEAR_VALUES.length; i++) {
			RegistryUtils.registerModel(GEAR_ITEMS[i], 0, "charset:gear#inventory_" + GEAR_VALUES[i]);
		}

		ModelLoader.setCustomStateMapper(blockGearbox, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation("charset:gearbox", "inventory");
			}
		});
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		TileHandCrankRenderer.INSTANCE.crankModel = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/hand_crank"), event.getMap());
		ModelGearbox.INSTANCE.modelInner = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/gearbox_inner"), event.getMap());
		ModelGearbox.INSTANCE.modelOuter = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/gearbox_outer"), event.getMap());
		ModelGearbox.INSTANCE.modelOuterOutputs = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/gearbox_outer_outputs"), event.getMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:gearbox", "inventory"), ModelGearbox.INSTANCE);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onBlockColor(ColorHandlerEvent.Block event) {
		event.getBlockColors().registerBlockColorHandler(GearboxColorHandler.INSTANCE, blockGearbox);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemColor(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(GearboxColorHandler.INSTANCE, itemGearbox);
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockAxle, "axle");
		RegistryUtils.register(event.getRegistry(), blockCreativeGenerator, "creative_generator_mechanical");
		RegistryUtils.register(event.getRegistry(), blockGearbox, "gearbox");
		RegistryUtils.register(event.getRegistry(), blockHandCrank, "hand_crank_mechanical");
		RegistryUtils.register(event.getRegistry(), blockSocket, "socket_mechanical");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemAxle, "axle");
		RegistryUtils.register(event.getRegistry(), itemCreativeGenerator, "creative_generator_mechanical");
		RegistryUtils.register(event.getRegistry(), itemGearbox, "gearbox");
		RegistryUtils.register(event.getRegistry(), itemHandCrank, "hand_crank_mechanical");
		RegistryUtils.register(event.getRegistry(), itemSocket, "socket_mechanical");

		for (int i = 0; i < GEAR_VALUES.length; i++) {
			RegistryUtils.register(event.getRegistry(), GEAR_ITEMS[i], "gear_mechanical_" + GEAR_VALUES[i]);
		}
	}
}
