/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.gates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import mcmultipart.multipart.MultipartRegistry;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.recipe.IRecipeObject;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.wires.WireFactory;
import pl.asie.charset.lib.wires.WireManager;
import pl.asie.charset.wires.ModCharsetWires;
import pl.asie.charset.wires.logic.WireSignalFactory;

@Mod(modid = ModCharsetGates.MODID, name = ModCharsetGates.NAME, version = ModCharsetGates.VERSION,
		dependencies = ModCharsetLib.DEP_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetGates {
	public static final String MODID = "CharsetGates";
	public static final String NAME = "CharsetGates";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.gates.ProxyClient", serverSide = "pl.asie.charset.gates.ProxyCommon")
	public static ProxyCommon proxy;
	@Instance(ModCharsetGates.MODID)
	public static ModCharsetGates INSTANCE;

	public static Configuration config;
	public static PacketRegistry packet;
	public static ItemGate itemGate;

	static final String[] gateMeta = new String[64]; // TODO: why 64 lol
	static final String[] gateUN = new String[64];
	static final Map<String, Integer> metaGate = new HashMap<String, Integer>();
	static final Set<ItemStack> gateStacks = new HashSet<ItemStack>();

	static final Map<String, Class<? extends PartGate>> gateParts = new HashMap<String, Class<? extends PartGate>>();
	static final Map<String, ResourceLocation> gateDefintions = new HashMap<String, ResourceLocation>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!ModCharsetLib.moduleEnabled(ModCharsetLib.MODULE_GATES))
			return;

		config = new Configuration(ModCharsetLib.instance.getConfigFile("gates.cfg"));
		itemGate = new ItemGate();
		GameRegistry.register(itemGate.setRegistryName("gate"));

		registerGate("nand", PartGateNAND.class, 0);
		registerGate("nor", PartGateNOR.class, 1);
		registerGate("xor", PartGateXOR.class, 2);
		registerGate("pulse_former", PartGatePulseFormer.class, 3);
		registerGate("multiplexer", PartGateMultiplexer.class, 4);
		// registerGate("rs_latch", PartGateRSLatch.class, 5);
		registerGate("buffer", PartGateBuffer.class, 6);

		MinecraftForge.EVENT_BUS.register(proxy);

		if (config.hasChanged()) {
			config.save();
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (!ModCharsetLib.moduleEnabled(ModCharsetLib.MODULE_GATES))
			return;

		packet = new PacketRegistry(ModCharsetGates.MODID);

		registerGateStack(ItemGate.getStack(new PartGateNOR().setInvertedSides(0b0001)), "sts", "scs", "sss");
		registerGateStack(ItemGate.getStack(new PartGateNAND().setInvertedSides(0b0001)), "wtw", "ccc", "sws");
		registerGateStack(ItemGate.getStack(new PartGateXOR()), "w w", "cwc", "scs");
		registerGateStack(ItemGate.getStack(new PartGateNOR()), "s s", "scs", "sss");
		registerGateStack(ItemGate.getStack(new PartGateNAND()), "w w", "ccc", "sws");
		registerGateStack(ItemGate.getStack(new PartGateXOR().setInvertedSides(0b0001)), "wtw", "cwc", "scs");
		registerGateStack(ItemGate.getStack(new PartGatePulseFormer()), "wcw", "cwc", "wws");
		registerGateStack(ItemGate.getStack(new PartGateMultiplexer()), "wcw", "csc", "wcw");
//		registerGateStack(ItemGate.getStack(new PartGateRSLatch()), "scs", "wsw", "scs");
		registerGateStack(ItemGate.getStack(new PartGateBuffer()));
		registerGateStack(ItemGate.getStack(new PartGateBuffer().setInvertedSides(0b0001)));

		if (config.hasChanged()) {
			config.save();
		}
	}

	public void registerGateStack(ItemStack stack, Object... recipe) {
		if (recipe.length > 0) {
			List<Object> data = new ArrayList<Object>();
			for (Object o : recipe) {
				data.add(o);
			}

			data.add('c');
			data.add(new ItemStack(Blocks.REDSTONE_TORCH));
			data.add('w');
			data.add(Loader.isModLoaded("CharsetWires") ? new IRecipeObject() {
				@Override
				public boolean matches(ItemStack stack) {
					if (stack != null && stack.getItem() == WireManager.ITEM) {
						return WireManager.ITEM.getFactory(stack).getRegistryName().getResourceDomain().equals("charsetwires");
					} else {
						return false;
					}
				}

				@Override
				public Object preview() {
					List<ItemStack> stacks = new ArrayList<>();
					for (WireFactory f : WireManager.REGISTRY.getValues()) {
						if (f.getRegistryName().getResourceDomain().equals("charsetwires")) {
							stacks.add(WireManager.ITEM.getStack(f, false));
							stacks.add(WireManager.ITEM.getStack(f, true));
						}
					}
					return stacks;
				}
			} : Items.REDSTONE);
			data.add('s');
			data.add(new ItemStack(Blocks.STONE_SLAB));
			GameRegistry.addRecipe(RecipeCharset.Builder.create(stack).shaped(data.toArray(new Object[data.size()])).build());
		}

		registerGateStack(stack);
	}

	public void registerGateStack(ItemStack stack) {
		if (stack != null && (stack.getItem() instanceof ItemGate))
			gateStacks.add(stack);
	}

	private void registerGate(String name, Class<? extends PartGate> clazz, int meta) {
		registerGate("charsetgates:gate_" + name, clazz, meta, "charsetgates:gatedefs/" + name,
				"part.charset.gate." + name);
	}

	public void registerGate(String name, Class<? extends PartGate> clazz, int meta, String gdLoc, String unl) {
		if (!config.getBoolean(name, "gates", true,"Enable/disable gate.")) {
			return;
		}

		gateParts.put(name, clazz);
		gateDefintions.put(name, new ResourceLocation(gdLoc + ".json"));
		gateMeta[meta] = name;
		gateUN[meta] = unl;
		metaGate.put(name, meta);
		MultipartRegistry.registerPart(clazz, name);
		ModCharsetLib.proxy.registerItemModel(itemGate, meta, name);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
