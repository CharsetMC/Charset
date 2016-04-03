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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
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

@Mod(modid = ModCharsetGates.MODID, name = ModCharsetGates.NAME, version = ModCharsetGates.VERSION,
		dependencies = ModCharsetLib.DEP_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetGates {
	public static final String MODID = "CharsetGates";
	public static final String NAME = "&";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.gates.ProxyClient", serverSide = "pl.asie.charset.gates.ProxyCommon")
	public static ProxyCommon proxy;

	public static PacketRegistry packet;
	public static ItemGate itemGate;
	public static ItemScrewdriver itemScrewdriver;

	static final String[] gateMeta = new String[64]; // TODO: why 64 lol
	static final String[] gateUN = new String[64];
	static final Map<String, Integer> metaGate = new HashMap<String, Integer>();
	static final Set<ItemStack> gateStacks = new HashSet<ItemStack>();

	static final Map<String, Class<? extends PartGate>> gateParts = new HashMap<String, Class<? extends PartGate>>();
	static final Map<String, ResourceLocation> gateDefintions = new HashMap<String, ResourceLocation>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		itemGate = new ItemGate();
		GameRegistry.register(itemGate.setRegistryName("gate"));

		itemScrewdriver = new ItemScrewdriver();
		GameRegistry.register(itemScrewdriver.setRegistryName("screwdriver"));

		registerGate("nand", PartGateNAND.class, 0);
		registerGate("nor", PartGateNOR.class, 1);
		registerGate("xor", PartGateXOR.class, 2);
		registerGate("pulse_former", PartGatePulseFormer.class, 3);
		registerGate("multiplexer", PartGateMultiplexer.class, 4);
		registerGate("rs_latch", PartGateRSLatch.class, 5);

		ModCharsetLib.proxy.registerItemModel(itemScrewdriver, 0, "charsetgates:screwdriver");

		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		packet = new PacketRegistry(ModCharsetGates.MODID);
		registerGateStack(ItemGate.getStack(new PartGateNOR().setInvertedSides(0b0001)), "scs", "scs", "sss");
		registerGateStack(ItemGate.getStack(new PartGateNAND().setInvertedSides(0b0001)), "wcw", "ccc", "sws");
		registerGateStack(ItemGate.getStack(new PartGateXOR()), "wsw", "cwc", "scs");
		registerGateStack(ItemGate.getStack(new PartGateNOR()), "sws", "scs", "sss");
		registerGateStack(ItemGate.getStack(new PartGateNAND()), "www", "ccc", "sws");
		registerGateStack(ItemGate.getStack(new PartGateXOR().setInvertedSides(0b0001)), "wcw", "cwc", "scs");
		registerGateStack(ItemGate.getStack(new PartGatePulseFormer()), "wcw", "cwc", "wws");
		registerGateStack(ItemGate.getStack(new PartGateMultiplexer()), "wcw", "csc", "wcw");
		registerGateStack(ItemGate.getStack(new PartGateRSLatch()), "scs", "wsw", "scs");

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemScrewdriver), "  i", "si ", "ws ", 'i', "ingotIron", 's', "stickWood",
				'w', new ItemStack(Blocks.wool, 1, EnumDyeColor.PINK.getMetadata())));
	}

	private void registerGateStack(ItemStack stack, Object... recipe) {
		List<Object> data = new ArrayList<Object>();
		for (Object o : recipe) {
			data.add(o);
		}

		data.add('c');
		data.add(new ItemStack(Blocks.redstone_torch));
		data.add('w');
		data.add(Loader.isModLoaded("CharsetWires") ? Item.getByNameOrId("CharsetWires:wire") : Items.redstone);
		data.add('s');
		data.add(new ItemStack(Blocks.stone_slab));
		GameRegistry.addRecipe(new ShapedOreRecipe(stack, data.toArray(new Object[data.size()])));

		registerGateStack(stack);
	}

	private void registerGateStack(ItemStack stack) {
		gateStacks.add(stack);
	}

	private void registerGate(String name, Class<? extends PartGate> clazz, int meta) {
		registerGate("charsetgates:gate_" + name, clazz, meta, "charsetgates:gatedefs/" + name,
				"part.charset.gate." + name);
	}

	public void registerGate(String name, Class<? extends PartGate> clazz, int meta, String gdLoc, String unl) {
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
