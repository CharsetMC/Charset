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

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.handlers.ShiftScrollHandler;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.recipe.IngredientGroup;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.wires.*;
import pl.asie.charset.shared.SimpleLogicShared;
import pl.asie.simplelogic.wires.logic.LogicWireProvider;
import pl.asie.simplelogic.wires.logic.WireRenderHandlerOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

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

	@CharsetModule.Configuration
	public static Configuration config;
	public static boolean useTESRs;

	@EventHandler
	public void loadConfig(CharsetLoadConfigEvent event) {
		useTESRs = ConfigUtils.getBoolean(config, "client", "forceWireTESRs", false, "Forces redstone cables to render using TESRs.", false);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wireProviders[0] = new LogicWireProvider(WireType.NORMAL, -1).setRegistryName(new ResourceLocation("charset:logic_wire_n"));
		for (int i = 0; i < 16; i++) {
			wireProviders[i + 1] = new LogicWireProvider(WireType.INSULATED, i).setRegistryName(new ResourceLocation("charset:logic_wire_i" + i));
		}
		wireProviders[17] = new LogicWireProvider(WireType.BUNDLED, -1).setRegistryName(new ResourceLocation("charset:logic_wire_b"));

		for (int i = 0; i < wireProviders.length; i++) {
			wireItems[i] = new ItemWire(wireProviders[i]);
			wireItems[i].setRegistryName(wireProviders[i].getRegistryName());
		}

		// configure creative tab
		SimpleLogicShared.TAB_ICON = new ItemStack(wireItems[0]);

		for (int i = 0; i < 16; i++) {
			IngredientGroup.register("simplelogic:wireInsulated", i, new ItemStack(wireItems[i + 1]));
		}
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		for (int i = 0; i < wireItems.length; i++) {
			RegistryUtils.register(event.getRegistry(), wireItems[i], wireItems[i].getRegistryName().getPath(), SimpleLogicShared.getTab());
		}
	}

	@SubscribeEvent
	public void onRegisterWires(RegistryEvent.Register<WireProvider> event) {
		for (int i = 0; i < wireProviders.length; i++) {
			RegistryUtils.register(event.getRegistry(), wireProviders[i], wireProviders[i].getRegistryName().getPath(), SimpleLogicShared.getTab());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		CharsetLibWires.instance.registerRenderer(wireProviders[17], new WireRenderHandlerOverlay(wireProviders[17]));
		CharsetLibWires.instance.registerRenderer(wireProviders[0], new IWireRenderContainer.Simple(new WireRenderHandlerDefault(wireProviders[0]) {
			@Override
			@SideOnly(Side.CLIENT)
			public boolean isDynamic() {
				return useTESRs;
			}
		}));
	}

	private void addWireOD(String name, Item i) {
		ItemStack nonFreestanding = new ItemStack(i, 1, 0);
		ItemStack freestanding = new ItemStack(i, 1, 1);

		OreDictionary.registerOre("wireLogic", i);
		OreDictionary.registerOre("wireLogicGrounded", nonFreestanding);
		OreDictionary.registerOre("wireLogicFreestanding", freestanding);
		OreDictionary.registerOre("wireLogic" + name, i);
		OreDictionary.registerOre("wireLogic" + name + "Grounded", nonFreestanding);
		OreDictionary.registerOre("wireLogic" + name + "Freestanding", freestanding);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Collection<Item> scrollableInsulated = new LinkedHashSet<>();
		for (int i = 0; i < 16; i++) {
			scrollableInsulated.add(wireItems[i + 1]);
		}

		ShiftScrollHandler.INSTANCE.register(new ShiftScrollProviderWire(scrollableInsulated, false));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollProviderWire(scrollableInsulated, true));

		addWireOD("Redstone", wireItems[0]);
		addWireOD("Bundled", wireItems[17]);

		for (int i = 0; i < 16; i++) {
			addWireOD("Insulated", wireItems[i + 1]);
			addWireOD(ColorUtils.getOreDictEntry("Insulated", EnumDyeColor.byMetadata(i)), wireItems[i + 1]);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
