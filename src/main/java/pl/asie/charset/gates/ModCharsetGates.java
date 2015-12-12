package pl.asie.charset.gates;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;

@Mod(modid = ModCharsetGates.MODID, name = ModCharsetGates.NAME, version = ModCharsetGates.VERSION,
	dependencies = "required-after:CharsetLib@" + ModCharsetGates.VERSION, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetGates {
	public static final String MODID = "CharsetGates";
	public static final String NAME = "&";
	public static final String VERSION = "@VERSION@";

	public static PacketRegistry packet;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

	@EventHandler
	public void init(FMLInitializationEvent event) {
		packet = new PacketRegistry(ModCharsetGates.MODID);
	}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
