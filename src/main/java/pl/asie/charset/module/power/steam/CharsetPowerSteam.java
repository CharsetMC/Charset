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

package pl.asie.charset.module.power.steam;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.modcompat.crafttweaker.Registry;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.power.steam.api.IMirror;
import pl.asie.charset.module.power.steam.api.IMirrorTarget;
import pl.asie.charset.module.power.steam.render.RenderMirror;
import pl.asie.charset.module.power.steam.render.RenderSteamParticle;
import pl.asie.charset.module.power.steam.render.RenderWaterBoiler;

import java.util.Objects;

@CharsetModule(
		name = "power.steam",
		description = "Steam power system",
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetPowerSteam {
	private static final ResourceLocation SCC_LOCATION = new ResourceLocation("charset", "steam_container");
	private static final ResourceLocation MCC_LOCATION = new ResourceLocation("charset", "mirror_container");

	@CapabilityInject(SteamChunkContainer.class)
	public static Capability<SteamChunkContainer> steamContainerCap;
	private static CapabilityProviderFactory<SteamChunkContainer> steamCapFactory;

	@CapabilityInject(SteamWorldContainer.class)
	public static Capability<SteamWorldContainer> steamWorldCap;
	private static CapabilityProviderFactory<SteamWorldContainer> steamWorldCapFactory;

	@CapabilityInject(MirrorChunkContainer.class)
	public static Capability<MirrorChunkContainer> mirrorContainerCap;
	private static CapabilityProviderFactory<MirrorChunkContainer> mirrorCapFactory;

	@CapabilityInject(IMirrorTarget.class)
	public static Capability<IMirrorTarget> MIRROR_TARGET;

	@CharsetModule.Configuration
	public static Configuration config;

	public static float BOILER_OUTPUT_MULTIPLIER;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	private static Fluid steam;

	public static BlockMirror blockMirror;
	public static ItemBlockBase itemMirror;

	public static BlockWaterBoiler blockWaterBoiler;
	public static ItemBlockBase itemWaterBoiler;

	@Mod.EventHandler
	public void onLoadConfig(CharsetLoadConfigEvent event) {
		BOILER_OUTPUT_MULTIPLIER = ConfigUtils.getFloat(config, "balance", "waterBoilerSteamMultiplier", 1.0f, 0.001f, 1000f, "The multiplier for the water boiler's steam output.", false);
	}

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(SteamChunkContainer.class, new SteamChunkContainerStorage(), SteamChunkContainer::new);
		CapabilityManager.INSTANCE.register(SteamWorldContainer.class, DummyCapabilityStorage.get(), SteamWorldContainer::new);
		CapabilityManager.INSTANCE.register(MirrorChunkContainer.class, DummyCapabilityStorage.get(), MirrorChunkContainer::new);
		CapabilityManager.INSTANCE.register(IMirrorTarget.class, DummyCapabilityStorage.get(), DummyMirrorTarget::new);

		FluidRegistry.registerFluid(steam = new Fluid("steam", new ResourceLocation("charset:blocks/steam"), new ResourceLocation("charset:blocks/steam"))
				.setDensity(-500).setGaseous(true).setViscosity(100).setUnlocalizedName("charset.steam").setTemperature(273 + 110));

		blockMirror = new BlockMirror();
		itemMirror = new ItemBlockMirror(blockMirror);

		blockWaterBoiler = new BlockWaterBoiler();
		itemWaterBoiler = new ItemBlockBase(blockWaterBoiler);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		RegistryUtils.register(TileMirror.class, "solar_mirror");
		RegistryUtils.register(TileWaterBoiler.class, "water_boiler");

		packet.registerPacket(0x01, PacketSpawnParticle.class);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPreInitClient(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new RenderMirror());
		MinecraftForge.EVENT_BUS.register(new RenderSteamParticle());
		MinecraftForge.EVENT_BUS.register(RenderWaterBoiler.INSTANCE);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onInitClient(FMLInitializationEvent event){
		ClientRegistry.bindTileEntitySpecialRenderer(TileWaterBoiler.class, RenderWaterBoiler.INSTANCE);
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
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().world != null) {
			Objects.requireNonNull(Minecraft.getMinecraft().world.getCapability(steamWorldCap, null)).getAllContainers().forEach(SteamChunkContainer::update);
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER) {
			Objects.requireNonNull(event.world.getCapability(steamWorldCap, null)).getAllContainers().forEach(SteamChunkContainer::update);
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			event.getWorld().addEventListener(new SteamWorldEventListener(event.getWorld()));
		}
	}

	@SubscribeEvent
	public void onStartWatching(ChunkWatchEvent.Watch event) {
		SteamChunkContainer c = event.getChunkInstance().getCapability(steamContainerCap, null);
		for (SteamParticle p : c.getParticles()) {
			if (!p.isInvalid()) {
				packet.sendTo(new PacketSpawnParticle(p), event.getPlayer());
			}
		}
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		Objects.requireNonNull(event.getChunk().getWorld().getCapability(steamWorldCap, null))
				.onChunkLoaded(event.getChunk());
	}

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		Objects.requireNonNull(event.getChunk().getWorld().getCapability(steamWorldCap, null))
				.onChunkUnloaded(event.getChunk());
	}

	@SubscribeEvent
	@SuppressWarnings("ConstantConditions")
	public void onAttachCapabilitiesWorld(AttachCapabilitiesEvent<World> event) {
		if (steamWorldCapFactory == null) {
			steamWorldCapFactory = new CapabilityProviderFactory<>(steamWorldCap);
		}

		event.addCapability(SCC_LOCATION, steamWorldCapFactory.create(new SteamWorldContainer()));
	}

	@SubscribeEvent
	@SuppressWarnings("ConstantConditions")
	public void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event) {
		if (event.getObject().getWorld() != null) {
			if (steamCapFactory == null) {
				steamCapFactory = new CapabilityProviderFactory<>(steamContainerCap);
				mirrorCapFactory = new CapabilityProviderFactory<>(mirrorContainerCap);
			}

			event.addCapability(SCC_LOCATION, steamCapFactory.create(new SteamChunkContainer(event.getObject())));
			event.addCapability(MCC_LOCATION, mirrorCapFactory.create(new MirrorChunkContainer(event.getObject())));
		}
	}
}
