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

package pl.asie.charset.module.power.steam;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.modcompat.crafttweaker.Registry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.power.steam.render.RenderMirror;

@CharsetModule(
		name = "power.steam",
		description = "Steam power system",
		profile = ModuleProfile.INDEV
)
public class CharsetPowerSteam {
	private static final ResourceLocation SCC_LOCATION = new ResourceLocation("charset", "steam_chunk_container");

	@CapabilityInject(SteamChunkContainer.class)
	public static Capability<SteamChunkContainer> chunkContainerCapability;
	private static CapabilityProviderFactory<SteamChunkContainer> factory;

	private static Fluid steam;

	public static BlockMirror blockMirror;
	public static ItemBlockBase itemMirror;

	public static BlockWaterBoiler blockWaterBoiler;
	public static ItemBlockBase itemWaterBoiler;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(SteamChunkContainer.class, DummyCapabilityStorage.get(), SteamChunkContainer::new);
		FluidRegistry.registerFluid(steam = new Fluid("steam", new ResourceLocation("charset:blocks/steam"), new ResourceLocation("charset:blocks/steam"))
				.setDensity(-500).setGaseous(true).setViscosity(100).setUnlocalizedName("charset.steam").setTemperature(273 + 110));

		blockMirror = new BlockMirror();
		itemMirror = new ItemBlockBase(blockMirror);

		blockWaterBoiler = new BlockWaterBoiler();
		itemWaterBoiler = new ItemBlockBase(blockWaterBoiler);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		RegistryUtils.register(TileMirror.class, "solar_mirror");
		RegistryUtils.register(TileWaterBoiler.class, "water_boiler");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPreInitClient(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new RenderMirror());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegistry(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemMirror, 0, "charset:solar_mirror");
		RegistryUtils.registerModel(itemWaterBoiler, 0, "charset:water_boiler");
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockMirror, "solar_mirror");
		RegistryUtils.register(event.getRegistry(), blockWaterBoiler, "water_boiler");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemMirror, "solar_mirror");
		RegistryUtils.register(event.getRegistry(), itemWaterBoiler, "water_boiler");
	}

	@SubscribeEvent
	@SuppressWarnings("ConstantConditions")
	public void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event) {
		if (event.getObject().getWorld() != null) {
			if (factory == null) {
				factory = new CapabilityProviderFactory<>(chunkContainerCapability);
			}

			event.addCapability(SCC_LOCATION, factory.create(new SteamChunkContainer(event.getObject())));
		}
	}
}
