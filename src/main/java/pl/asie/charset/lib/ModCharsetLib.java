package pl.asie.charset.lib;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by asie on 11/12/15.
 */
@Mod(modid = ModCharsetLib.MODID, name = ModCharsetLib.NAME, version = ModCharsetLib.VERSION, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetLib {
	public static final String UPDATE_URL = "http://charset.asie.pl/update.json";
	public static final String MODID = "CharsetLib";
	public static final String NAME = "‽";
	public static final String VERSION = "@VERSION@";

	@Mod.Instance(value = ModCharsetLib.MODID)
	public static ModCharsetLib instance;

	@SidedProxy(clientSide = "pl.asie.charset.lib.ProxyClient", serverSide = "pl.asie.charset.lib.ProxyCommon")
	public static ProxyCommon proxy;

	public static IconCharset charsetIconItem;

	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("charset") {
		@Override
		public Item getTabIconItem() {
			return charsetIconItem;
		}
	};
	public static Logger logger;

	private File configurationDirectory;

	public File getConfigFile(String filename) {
		return new File(configurationDirectory, filename);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(MODID);

		configurationDirectory = new File(event.getModConfigurationDirectory(), "charset");
		if (!configurationDirectory.exists()) {
			configurationDirectory.mkdir();
		}

		charsetIconItem = new IconCharset();
		GameRegistry.registerItem(charsetIconItem, "icon");

		proxy.registerItemModel(charsetIconItem, 0, "charsetlib:icon");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
