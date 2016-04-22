package pl.asie.charset.wires;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import mcmultipart.multipart.MultipartRegistry;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.wires.logic.PartWireProvider;

@Mod(modid = ModCharsetWires.MODID, name = ModCharsetWires.NAME, version = ModCharsetWires.VERSION,
		dependencies = ModCharsetLib.DEP_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetWires {
	public static final String MODID = "CharsetWires";
	public static final String NAME = "+";
	public static final String VERSION = "@VERSION@";

	public static PacketRegistry packet;

	@SidedProxy(clientSide = "pl.asie.charset.wires.ProxyClient", serverSide = "pl.asie.charset.wires.ProxyCommon")
	public static ProxyCommon proxy;

	public static ItemWire wire;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MultipartRegistry.registerPartFactory(new PartWireProvider(), "charsetwires:wire");

		wire = new ItemWire();
		GameRegistry.register(wire.setRegistryName("wire"));

		MinecraftForge.EVENT_BUS.register(proxy);

		for (int i = 0; i < 2 * 18; i++) {
			ModCharsetLib.proxy.registerItemModel(wire, i, "charsetwires:wire");
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		packet = new PacketRegistry(ModCharsetWires.MODID);

		// Temporary recipes
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 16, 0),
				"r r", "rir", "r r", 'r', "dustRedstone", 'i', "ingotIron"));

		for (int i = 0; i < 16; i++) {
			OreDictionary.registerOre("charsetWireInsulated", new ItemStack(wire, 1, (i + 1) << 1));
			OreDictionary.registerOre("charsetWireInsulatedFreestanding", new ItemStack(wire, 1, (i + 1) << 1));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 8, 2 + (i << 1)),
					"ddd", "dwd", "ddd", 'd', new ItemStack(wire, 1, 0), 'w', new ItemStack(Blocks.WOOL, 1, i)));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 8, 3 + (i << 1)),
					"ddd", "dwd", "ddd", 'd', new ItemStack(wire, 1, 1), 'w', new ItemStack(Blocks.WOOL, 1, i)));
		}

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 1, 34),
				"sws", "www", "sws", 'w', "charsetWireInsulated", 's', Items.STRING
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 1, 35),
				"sws", "www", "sws", 'w', "charsetWireInsulatedFreestanding", 's', Items.STRING
		));

		for (int i = 0; i < 18; i++) {
			GameRegistry.addShapelessRecipe(new ItemStack(wire, 1, 0 + (i << 1)), new ItemStack(wire, 1, 1 + (i << 1)));
			GameRegistry.addShapelessRecipe(new ItemStack(wire, 1, 1 + (i << 1)), new ItemStack(wire, 1, 0 + (i << 1)));
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
