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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "tools.engineering",
		description = "Engineer's tools: Stopwatch, Signal Meter, Tuning Fork, Tape Measure.",
		profile = ModuleProfile.TESTING

)
public class CharsetToolsEngineering {
	//public static ItemTapeMeasure tapeMeasure;
	public static ItemStopwatch stopwatch;
	public static ItemSignalMeter signalMeter;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	@CapabilityInject(StopwatchTracker.class)
	static Capability<StopwatchTracker> stopwatchTrackerCap;
	private static final ResourceLocation stopwatchTrackerLoc = new ResourceLocation("charset", "stopwatchTracker");
	private static CapabilityProviderFactory<StopwatchTracker> stopwatchTrackerProvider;

	@CapabilityInject(ISignalMeterTracker.class)
	static Capability<ISignalMeterTracker> meterTrackerCap;
	private static final ResourceLocation meterTrackerLoc = new ResourceLocation("charset", "signal_meter_tracker");
	private static CapabilityProviderFactory<ISignalMeterTracker> meterTrackerProvider;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		stopwatch = new ItemStopwatch();
		signalMeter = new ItemSignalMeter();
		//tapeMeasure = new ItemTapeMeasure();

		CapabilityManager.INSTANCE.register(StopwatchTracker.class, DummyCapabilityStorage.get(), StopwatchTracker::new);
		CapabilityManager.INSTANCE.register(ISignalMeterTracker.class, DummyCapabilityStorage.get(), SignalMeterTracker::new);

		CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(ISignalMeterData.class).register(SignalMeterDataDummy.class, SignalMeterDataDummy::new);
		CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(ISignalMeterData.class).register(SignalMeterDataRedstone.class, SignalMeterDataRedstone::new);
		SignalMeterProviderHandler.INSTANCE.registerRemoteProvider(new SignalMeterDataRedstone.Provider(), false);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPreInitClient(FMLPreInitializationEvent event) {
		//MinecraftForge.EVENT_BUS.register(TapeMeasureRenderer.INSTANCE);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketSignalMeterData.class);

		MinecraftForge.EVENT_BUS.register(new SignalMeterTrackerEventHandler());
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), stopwatch, "stopwatch");
		RegistryUtils.register(event.getRegistry(), signalMeter, "signal_meter");
		//RegistryUtils.register(event.getRegistry(), tapeMeasure, "tape_measure");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		ModelSignalMeter.WHITE = event.getMap().registerSprite(new ResourceLocation("charset", "misc/white"));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(stopwatch, 0, "charset:stopwatch");
		RegistryUtils.registerModel(signalMeter, 0, "charset:signal_meter");
		//RegistryUtils.registerModel(tapeMeasure, 0, "charset:tape_measure");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void postModelBake(ModelBakeEvent event) {
		try {
			ModelResourceLocation mrl = new ModelResourceLocation("charset:signal_meter", "inventory");
			event.getModelRegistry().putObject(mrl, new ModelSignalMeter(event.getModelRegistry().getObject(mrl)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void attachCapsWorld(AttachCapabilitiesEvent<World> event) {
		if (!event.getObject().isRemote) {
			if (stopwatchTrackerProvider == null) {
				stopwatchTrackerProvider = new CapabilityProviderFactory<>(stopwatchTrackerCap);
			}

			event.addCapability(stopwatchTrackerLoc, stopwatchTrackerProvider.create(new StopwatchTracker(event.getObject())));
		}
	}

	@SubscribeEvent
	public void attachCapsPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (meterTrackerProvider == null) {
			meterTrackerProvider = new CapabilityProviderFactory<>(meterTrackerCap);
		}

		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(meterTrackerLoc, meterTrackerProvider.create(new SignalMeterTracker()));
		}
	}
}
