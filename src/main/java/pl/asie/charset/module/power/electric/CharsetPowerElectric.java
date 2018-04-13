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

package pl.asie.charset.module.power.electric;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.wires.ItemWire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.module.audio.transport.WireProviderAudioCable;
import pl.asie.charset.module.power.steam.SteamChunkContainer;

@CharsetModule(
		name = "power.electric",
		dependencies = {"lib.wires"},
		description = "Electric, FE-compatible power system",
		profile = ModuleProfile.INDEV
)
public class CharsetPowerElectric {
	private WireProviderElectric electricWire;
	private ItemWire electricWireWire;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		electricWire = new WireProviderElectric();
		electricWireWire = new ItemWire(electricWire);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), electricWireWire, "electric_wire");
	}

	@SubscribeEvent
	public void registerWires(RegistryEvent.Register<WireProvider> event) {
		RegistryUtils.register(event.getRegistry(), electricWire, "electric_wire");
	}
}
