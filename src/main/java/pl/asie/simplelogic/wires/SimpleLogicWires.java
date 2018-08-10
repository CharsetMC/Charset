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

	private static WireProvider[] wireProviders = new WireProvider[34];
	private static ItemWire[] wireItems = new ItemWire[wireProviders.length];

	@CharsetModule.Configuration
	public static Configuration config;
	public static boolean useTESRs;
	public static boolean enableRedstoneCables, enableInsulatedCables, enableBundledCables, enableColoredBundledCables;

	@EventHandler
	public void loadConfig(CharsetLoadConfigEvent event) {
		useTESRs = ConfigUtils.getBoolean(config, "client", "forceWireTESRs", false, "Forces redstone cables to render using TESRs.", false);
		enableRedstoneCables = ConfigUtils.getBoolean(config, "general", "enableRedstoneCables", true, "Should redstone cables be enabled? Note that recipes will not be currently adapted.", true);
		enableInsulatedCables = ConfigUtils.getBoolean(config, "general", "enableInsulatedCables", true, "Should insulated cables be enabled? Note that recipes will not be currently adapted.", true);
		enableBundledCables = ConfigUtils.getBoolean(config, "general", "enableBundledCables", true, "Should bundled cables be enabled? Note that recipes will not be currently adapted.", true);
		enableColoredBundledCables = ConfigUtils.getBoolean(config, "general", "enableColoredBundledCables", true, "Should colored bundled cables be enabled? Note that recipes will not be currently adapted.", true);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (enableRedstoneCables) {
			wireProviders[0] = new LogicWireProvider(WireType.NORMAL, -1).setRegistryName(new ResourceLocation("charset:logic_wire_n"));
		}
		if (enableInsulatedCables) {
			for (int i = 0; i < 16; i++) {
				wireProviders[i + 1] = new LogicWireProvider(WireType.INSULATED, i).setRegistryName(new ResourceLocation("charset:logic_wire_i" + i));
			}
		}
		if (enableBundledCables) {
			wireProviders[17] = new LogicWireProvider(WireType.BUNDLED, -1).setRegistryName(new ResourceLocation("charset:logic_wire_b"));
		}
		if (enableColoredBundledCables) {
			for (int i = 0; i < 16; i++) {
				wireProviders[i + 18] = new LogicWireProvider(WireType.BUNDLED, i).setRegistryName(new ResourceLocation("charset:logic_wire_bc" + i));
			}
		}

		boolean setIcon = false;
		for (int i = 0; i < wireProviders.length; i++) {
			if (wireProviders[i] != null) {
				wireItems[i] = new ItemWire(wireProviders[i]);
				wireItems[i].setRegistryName(wireProviders[i].getRegistryName());
				if (!setIcon) {
					SimpleLogicShared.TAB_ICON = new ItemStack(wireItems[i]);
					setIcon = true;
				}
			}
		}

		if (enableInsulatedCables) {
			for (int i = 0; i < 16; i++) {
				IngredientGroup.register("simplelogic:wireInsulated", i, new ItemStack(wireItems[i + 1]));
			}
		}
		if (enableColoredBundledCables) {
			for (int i = 0; i < 16; i++) {
				IngredientGroup.register("simplelogic:wireBundledColored", i, new ItemStack(wireItems[i + 18]));
			}
		}
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		for (int i = 0; i < wireItems.length; i++) {
			if (wireItems[i] != null) {
				RegistryUtils.register(event.getRegistry(), wireItems[i], wireItems[i].getRegistryName().getPath(), SimpleLogicShared.getTab());
			}
		}
	}

	@SubscribeEvent
	public void onRegisterWires(RegistryEvent.Register<WireProvider> event) {
		for (int i = 0; i < wireProviders.length; i++) {
			if (wireProviders[i] != null) {
				RegistryUtils.register(event.getRegistry(), wireProviders[i], wireProviders[i].getRegistryName().getPath(), SimpleLogicShared.getTab());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		if (enableRedstoneCables) {
			CharsetLibWires.instance.registerRenderer(wireProviders[0], new IWireRenderContainer.Simple(new WireRenderHandlerDefault(wireProviders[0]) {
				@Override
				@SideOnly(Side.CLIENT)
				public boolean isDynamic() {
					return useTESRs;
				}
			}));
		}
		if (enableBundledCables) {
			CharsetLibWires.instance.registerRenderer(wireProviders[17], new WireRenderHandlerOverlay(wireProviders[17], false));
		}
		if (enableColoredBundledCables) {
			for (int i = 0; i < 16; i++) {
				CharsetLibWires.instance.registerRenderer(wireProviders[18 + i], new WireRenderHandlerOverlay(wireProviders[18 + i], true));
			}
		}
	}

	private void addWireOD(String name, Item i) {
		if (i == null) return;

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
		if (enableInsulatedCables) {
			Collection<Item> scrollableInsulated = new LinkedHashSet<>();
			for (int i = 0; i < 16; i++) {
				scrollableInsulated.add(wireItems[i + 1]);
			}

			ShiftScrollHandler.INSTANCE.register(new ShiftScrollProviderWire(scrollableInsulated, false));
			ShiftScrollHandler.INSTANCE.register(new ShiftScrollProviderWire(scrollableInsulated, true));
		}
		if (enableColoredBundledCables) {
			Collection<Item> scrollableInsulated = new LinkedHashSet<>();
			for (int i = 0; i < 16; i++) {
				scrollableInsulated.add(wireItems[i + 18]);
			}

			ShiftScrollHandler.INSTANCE.register(new ShiftScrollProviderWire(scrollableInsulated, false));
			ShiftScrollHandler.INSTANCE.register(new ShiftScrollProviderWire(scrollableInsulated, true));
		}

		addWireOD("Redstone", wireItems[0]);
		addWireOD("Bundled", wireItems[17]);

		for (int i = 0; i < 16; i++) {
			addWireOD("Insulated", wireItems[i + 1]);
			addWireOD(ColorUtils.getOreDictEntry("Insulated", EnumDyeColor.byMetadata(i)), wireItems[i + 1]);
			addWireOD("BundledColored", wireItems[i + 18]);
			addWireOD(ColorUtils.getOreDictEntry("BundledColored", EnumDyeColor.byMetadata(i)), wireItems[i + 1]);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
