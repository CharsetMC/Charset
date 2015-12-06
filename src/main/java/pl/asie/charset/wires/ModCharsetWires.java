package pl.asie.charset.wires;

import net.minecraft.item.Item;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;

@Mod(modid = ModCharsetWires.MODID, name = ModCharsetWires.NAME, version = ModCharsetWires.VERSION,
	dependencies = "required-after:CharsetLib@" + ModCharsetWires.VERSION, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetWires {
	public static final String MODID = "CharsetWires";
	public static final String NAME = "+";
	public static final String VERSION = "@VERSION@";

	public static PacketRegistry packet;

	@SidedProxy(clientSide = "pl.asie.charset.wires.ProxyClient", serverSide = "pl.asie.charset.wires.ProxyCommon")
	public static ProxyCommon proxy;

	public static BlockWire wire;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		wire = new BlockWire();
		GameRegistry.registerBlock(wire, ItemWire.class, "wire");

		MinecraftForge.EVENT_BUS.register(proxy);

		for (int i = 0; i < 2 * 18; i++) {
			ModCharsetLib.proxy.registerItemModel(Item.getItemFromBlock(wire), i, "charsetwires:wire");
		}
    }

	@EventHandler
	public void init(FMLInitializationEvent event) {
		packet = new PacketRegistry(ModCharsetWires.MODID);

		GameRegistry.registerTileEntity(TileWire.class, "CharsetWires:wire");
	}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
