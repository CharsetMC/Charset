/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.wires.CharsetLibWires;
import pl.asie.charset.lib.wires.ItemWire;
import pl.asie.charset.lib.wires.WireProvider;

@CharsetModule(
		name = "power.electric",
		dependencies = {"lib.wires"},
		description = "Electric, FE-compatible power system",
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetPowerElectric {
	private WireProviderElectric wireElectric;
	private ItemWire itemWireElectric;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		wireElectric = new WireProviderElectric();
		itemWireElectric = new ItemWire(wireElectric);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemWireElectric, "electric_wire");
	}

	@SubscribeEvent
	public void registerWires(RegistryEvent.Register<WireProvider> event) {
		RegistryUtils.register(event.getRegistry(), wireElectric, "electric_wire");
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		CharsetLibWires.instance.registerRenderer(wireElectric, new WireElectricOverlay(wireElectric));
	}
}
