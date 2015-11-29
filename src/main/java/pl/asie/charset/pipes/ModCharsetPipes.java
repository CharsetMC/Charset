package pl.asie.charset.pipes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;

@Mod(modid = ModCharsetPipes.MODID, name = ModCharsetPipes.NAME, version = ModCharsetPipes.VERSION)
public class ModCharsetPipes {
	public static final String MODID = "CharsetPipes";
	public static final String NAME = "|";
	public static final String VERSION = "0.1.0";
	public static final Random RANDOM = new Random();

	public static final double PIPE_TESR_DISTANCE = 64.0D;

	public static PacketRegistry packet;

    @SidedProxy(clientSide = "pl.asie.charset.pipes.ProxyClient", serverSide = "pl.asie.charset.pipes.ProxyCommon")
    public static ProxyCommon proxy;

	public static Block pipeBlock, shifterBlock;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		pipeBlock = new BlockPipe();
		ModCharsetLib.proxy.registerBlock(pipeBlock, "pipe");
		GameRegistry.registerTileEntity(TilePipe.class, "CharsetPipes:pipe");

		shifterBlock = new BlockShifter();
		ModCharsetLib.proxy.registerBlock(shifterBlock, "shifter");
		GameRegistry.registerTileEntity(TileShifter.class, "CharsetPipes:shifter");
    }

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();
		packet = new PacketRegistry(ModCharsetPipes.MODID);

		packet.registerPacket(0x01, PacketItemUpdate.class);

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(shifterBlock),
				"cPc",
				"c^c",
				"crc",
				'c', Blocks.cobblestone, 'P', Blocks.piston, 'r', Items.comparator, '^', Items.arrow);

		ModCharsetLib.proxy.registerRecipeShaped(new ItemStack(pipeBlock, 8),
				"mgm",
				'g', Blocks.glass, 'm', Blocks.obsidian);
	}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
