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

package pl.asie.simplelogic.gates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.handlers.ShiftScrollHandler;
import pl.asie.simplelogic.gates.logic.*;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "simplelogic.gates",
		profile = ModuleProfile.EXPERIMENTAL,
		description = "Simple logic gates."
)
public class SimpleLogicGates {
	public static final String MODID = "CharsetGates";
	public static final String NAME = "CharsetGates";
	public static final String VERSION = "@VERSION@";

	@CharsetModule.SidedProxy(clientSide = "pl.asie.simplelogic.gates.ProxyClient", serverSide = "pl.asie.simplelogic.gates.ProxyCommon")
	public static ProxyCommon proxy;
	@CharsetModule.Instance(SimpleLogicGates.MODID)
	public static SimpleLogicGates INSTANCE;

	public static ProxyMultipart proxyMultipart = new ProxyMultipart();

	@CharsetModule.Configuration
	public static Configuration config;
	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static BlockGate blockGate;
	public static ItemGate itemGate;

	static final BiMap<ResourceLocation, Class<? extends GateLogic>> logicClasses = HashBiMap.create();
	static final Map<ResourceLocation, String> logicUns = new HashMap<>();
	static final Map<ResourceLocation, ResourceLocation> logicDefinitions = new HashMap<>();

	static final Set<ItemStack> gateStacks = new HashSet<ItemStack>();

	public static ResourceLocation getId(GateLogic logic) {
		return logicClasses.inverse().get(logic.getClass());
	}

	@SubscribeEvent
	public void onRegisterBlock(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockGate, "logic_gate");
	}

	@SubscribeEvent
	public void onRegisterItem(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemGate, "logic_gate");
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (Loader.isModLoaded("mcmultipart")) {
			proxyMultipart = new ProxyMultipartPresent();
		}

		blockGate = new BlockGate();
		itemGate = new ItemGate(blockGate);

		registerGate(new ResourceLocation("simplelogic:nand"), GateLogicNAND.class);
		registerGate(new ResourceLocation("simplelogic:nor"), GateLogicNOR.class);
		registerGate(new ResourceLocation("simplelogic:xor"), GateLogicXOR.class);
		registerGate(new ResourceLocation("simplelogic:multiplexer"), GateLogicMultiplexer.class);
		registerGate(new ResourceLocation("simplelogic:buffer"), GateLogicBuffer.class);
		registerGate(new ResourceLocation("simplelogic:pulse_former"), GateLogicPulseFormer.class);
		MinecraftForge.EVENT_BUS.register(proxy);

		if (config.hasChanged()) {
			config.save();
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(PartGate.class, "logic_gate");
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(itemGate));

		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicNOR()).setInvertedSides(0b0001)));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicNAND()).setInvertedSides(0b0001)));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicXOR())));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicNOR())));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicNAND())));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicXOR()).setInvertedSides(0b0001)));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicMultiplexer())));
		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicPulseFormer())));
//		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicBuffer())));
//		registerGateStack(ItemGate.getStack(new PartGate(new GateLogicBuffer()).setInvertedSides(0b0001)));
//		registerGateStack(ItemGate.getStack(new PartGateRSLatch()), "scs", "wsw", "scs"); */

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
	}

	public void registerGateStack(ItemStack stack) {
		if (stack != null && (stack.getItem() instanceof ItemGate)) {
			gateStacks.add(stack);
		}
	}

	private void registerGate(ResourceLocation name, Class<? extends GateLogic> clazz) {
		registerGate(name, clazz, new ResourceLocation(name.getResourceDomain(), "gatedefs/" + name.getResourcePath() + ".json"),
				"tile." + name.getResourceDomain() + ".gate." + name.getResourcePath());
	}

	public void registerGate(ResourceLocation name, Class<? extends GateLogic> clazz, ResourceLocation gdLoc, String unl) {
		// TODO: Make this config work
		/* if (!config.getBoolean(name.toString(), "gates", true,"Enable/disable gate.")) {
			return;
		} */

		logicClasses.put(name, clazz);
		logicDefinitions.put(name, gdLoc);
		logicUns.put(name, unl);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
