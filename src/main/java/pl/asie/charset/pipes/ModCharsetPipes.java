package pl.asie.charset.pipes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import mcmultipart.multipart.MultipartRegistry;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;

@Mod(modid = ModCharsetPipes.MODID, name = ModCharsetPipes.NAME, version = ModCharsetPipes.VERSION,
		dependencies = ModCharsetLib.DEP_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetPipes {
	public static final String MODID = "CharsetPipes";
	public static final String NAME = "|";
	public static final String VERSION = "@VERSION@";
	public static final Random RANDOM = new Random();

	public static final double PIPE_TESR_DISTANCE = 64.0D;

	public static PacketRegistry packet;

	@SidedProxy(clientSide = "pl.asie.charset.pipes.ProxyClient", serverSide = "pl.asie.charset.pipes.ProxyCommon")
	public static ProxyCommon proxy;

	public static Block shifterBlock;
	public static Item itemPipe;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		itemPipe = new ItemPipe();
		GameRegistry.registerItem(itemPipe, "pipe");
		MultipartRegistry.registerPart(PartPipe.class, "CharsetPipes:pipe");

		shifterBlock = new BlockShifter();
		ModCharsetLib.proxy.registerBlock(shifterBlock, "shifter");
		GameRegistry.registerTileEntity(TileShifter.class, "CharsetPipes:shifter");

		ModCharsetLib.proxy.registerItemModel(itemPipe, 0, "charsetpipes:pipe");
		ModCharsetLib.proxy.registerItemModel(shifterBlock, 0, "charsetpipes:shifter");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();

		packet = new PacketRegistry(ModCharsetPipes.MODID);
		packet.registerPacket(0x01, PacketItemUpdate.class);
		packet.registerPacket(0x02, PacketPipeSyncRequest.class);

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(shifterBlock),
				"cPc",
				"c^c",
				"crc",
				'c', Blocks.cobblestone, 'P', Blocks.piston, 'r', Items.redstone, '^', Items.arrow);

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(itemPipe, 8),
				"mgm",
				'g', Blocks.glass, 'm', Blocks.obsidian);

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(itemPipe, 8),
				"m",
				"g",
				"m",
				'g', Blocks.glass, 'm', Blocks.obsidian);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
