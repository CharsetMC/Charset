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

package pl.asie.charset.pipes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.pipes.pipe.*;
import pl.asie.charset.pipes.shifter.BlockShifter;
import pl.asie.charset.pipes.shifter.ShifterImpl;
import pl.asie.charset.pipes.shifter.ShifterStorage;
import pl.asie.charset.pipes.shifter.TileShifter;

@Mod(modid = ModCharsetPipes.MODID, name = ModCharsetPipes.NAME, version = ModCharsetPipes.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetPipes {
	public static final String MODID = "charsetpipes";
	public static final String NAME = "|";
	public static final String VERSION = "@VERSION@";
	public static final Random RANDOM = new Random();

	public static final double PIPE_TESR_DISTANCE = 64.0D;

	public static PacketRegistry packet;

	@SidedProxy(clientSide = "pl.asie.charset.pipes.ProxyClient", serverSide = "pl.asie.charset.pipes.ProxyCommon")
	public static ProxyCommon proxy;

	@CapabilityInject(IShifter.class)
	public static Capability<IShifter> CAP_SHIFTER;

	public static Block shifterBlock;
	public static Block blockPipe;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(IShifter.class, new ShifterStorage(), ShifterImpl.class);

		blockPipe = new BlockPipe();
		ModCharsetLib.proxy.registerBlock(blockPipe, "pipe");
		GameRegistry.registerTileEntity(TilePipe.class, "CharsetPipes:pipe");

		shifterBlock = new BlockShifter();
		ModCharsetLib.proxy.registerBlock(shifterBlock, "shifter");
		GameRegistry.registerTileEntity(TileShifter.class, "CharsetPipes:shifter");

		ModCharsetLib.proxy.registerItemModel(blockPipe, 0, "charsetpipes:pipe_item");
		ModCharsetLib.proxy.registerItemModel(shifterBlock, 0, "charsetpipes:shifter");

		MinecraftForge.EVENT_BUS.register(proxy);

		FMLInterModComms.sendMessage("charsetlib", "addCarry", blockPipe.getRegistryName());
		FMLInterModComms.sendMessage("charsetlib", "addCarry", shifterBlock.getRegistryName());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();

		packet = new PacketRegistry(ModCharsetPipes.MODID);
		packet.registerPacket(0x01, PacketItemUpdate.class);
		packet.registerPacket(0x02, PacketPipeSyncRequest.class);
		packet.registerPacket(0x03, PacketFluidUpdate.class);

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(shifterBlock),
				"cPc",
				"c^c",
				"crc",
				'c', "cobblestone", 'P', Blocks.PISTON, 'r', "dustRedstone", '^', Items.ARROW);

		if (!Loader.isModLoaded("BuildCraft|Transport")) {
			ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(blockPipe, 8),
					"mgm",
					'g', "blockGlassColorless", 'm', "obsidian");
		}

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(blockPipe, 8),
				"m",
				"g",
				"m",
				'g', "blockGlassColorless", 'm', "obsidian");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		TileShifter.registerVanillaExtractionHandlers();
	}
}
