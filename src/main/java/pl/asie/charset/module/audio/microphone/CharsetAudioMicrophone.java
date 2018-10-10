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

package pl.asie.charset.module.audio.microphone;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;

// PRAISE BTM
@CharsetModule(
		name = "audio.microphone",
		description = "Microphones!",
		profile = ModuleProfile.INDEV
)
public class CharsetAudioMicrophone {
	@CapabilityInject(IWirelessAudioReceiver.class)
	public static Capability<IWirelessAudioReceiver> WIRELESS_AUDIO_RECEIVER;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static BlockWirelessReceiver blockWirelessReceiver;
	public static ItemBlock itemWirelessReceiver;
	public static ItemMicrophone itemMicrophone;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(IWirelessAudioReceiver.class, DummyCapabilityStorage.get(), () -> (a, b) -> {});

		blockWirelessReceiver = new BlockWirelessReceiver();
		itemWirelessReceiver = new ItemBlockBase(blockWirelessReceiver);
		itemMicrophone = new ItemMicrophone();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileWirelessReceiver.class, "audio_wireless_receiver");

		packet.registerPacket(0x01, PacketSendDataTile.class);
	}

	@SideOnly(Side.CLIENT)
	@Mod.EventHandler
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new MicrophoneEventHandler());
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockWirelessReceiver, "audio_wireless_receiver");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemWirelessReceiver, "audio_wireless_receiver");
		RegistryUtils.register(event.getRegistry(), itemMicrophone, "audio_microphone");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRegisterModels(ModelRegistryEvent event) {
		// TODO
	}
}
