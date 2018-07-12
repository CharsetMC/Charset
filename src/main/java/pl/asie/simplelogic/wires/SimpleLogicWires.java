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

package pl.asie.simplelogic.wires;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.wires.ItemWire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.shared.SimpleLogicShared;
import pl.asie.simplelogic.wires.logic.LogicWireProvider;

@CharsetModule(
		name = "simplelogic.wires",
		description = "Simple wires.",
		dependencies = {"lib.wires"},
		profile = ModuleProfile.TESTING
)
public class SimpleLogicWires {
	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static WireProvider[] wireProviders = new WireProvider[18];
	public static ItemWire[] wireItems = new ItemWire[18];

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wireProviders[0] = new LogicWireProvider(WireType.NORMAL, -1).setRegistryName(new ResourceLocation("charset:simplelogic_wire_n"));
		for (int i = 0; i < 16; i++) {
			wireProviders[i + 1] = new LogicWireProvider(WireType.INSULATED, i).setRegistryName(new ResourceLocation("charset:simplelogic_wire_i" + i));
		}
		wireProviders[17] = new LogicWireProvider(WireType.BUNDLED, -1).setRegistryName(new ResourceLocation("charset:simplelogic_wire_b"));

		for (int i = 0; i < wireProviders.length; i++) {
			wireItems[i] = new ItemWire(wireProviders[i]);
			wireItems[i].setRegistryName(wireProviders[i].getRegistryName());
		}

		// configure creative tab
		SimpleLogicShared.TAB_ICON = new ItemStack(wireItems[17]);
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		for (int i = 0; i < wireItems.length; i++) {
			RegistryUtils.register(event.getRegistry(), wireItems[i], wireItems[i].getRegistryName().getResourcePath(), SimpleLogicShared.getTab());
		}
	}

	@SubscribeEvent
	public void onRegisterWires(RegistryEvent.Register<WireProvider> event) {
		for (int i = 0; i < wireProviders.length; i++) {
			RegistryUtils.register(event.getRegistry(), wireProviders[i], wireProviders[i].getRegistryName().getResourcePath(), SimpleLogicShared.getTab());
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
/*		// Temporary recipes
		GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(wireFactories[0], false, 8))
				.shaped(" r ", "rir", " r ", 'r', "dustRedstone", 'i', "ingotIron")
				.build());

		for (int i = 0; i < 16; i++) {
			GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(wireFactories[i + 1], false, 8))
					.shaped("ddd", "dwd", "ddd", 'd', new RecipeObjectWire(wireFactories[0], false), 'w', new ItemStack(Blocks.WOOL, 1, i))
					.build());
			GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(wireFactories[i + 1], true, 8))
					.shaped("ddd", "dwd", "ddd", 'd', new RecipeObjectWire(wireFactories[0], true), 'w', new ItemStack(Blocks.WOOL, 1, i))
					.build());
		}

		GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(wireFactories[17], false, 1))
				.shaped("sws", "www", "sws", 'w', new RecipeObjectSignalWire(WireType.INSULATED, false), 's', Items.STRING)
				.build());
		GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(wireFactories[17], true, 1))
				.shaped("sws", "www", "sws", 'w', new RecipeObjectSignalWire(WireType.INSULATED, true), 's', Items.STRING)
				.build()); */
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
