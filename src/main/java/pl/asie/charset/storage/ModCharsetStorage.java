package pl.asie.charset.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.storage.gui.StorageGuiHandler;

@Mod(modid = ModCharsetStorage.MODID, name = ModCharsetStorage.NAME, version = ModCharsetStorage.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetStorage {
	public static final String MODID = "CharsetStorage";
	public static final String NAME = "S";
	public static final String VERSION = "@VERSION@";

    @Mod.Instance(MODID)
    public static ModCharsetStorage instance;

    @SidedProxy(clientSide = "pl.asie.charset.storage.ProxyClient", serverSide = "pl.asie.charset.storage.ProxyCommon")
    public static ProxyCommon proxy;

	public static Logger logger;

    public static BlockBackpack backpackBlock;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetStorage.MODID);

        backpackBlock = new BlockBackpack();
        GameRegistry.registerBlock(backpackBlock, ItemBackpack.class, "backpack");

        ModCharsetLib.proxy.registerItemModel(backpackBlock, 0, "charsetstorage:backpack");

        MinecraftForge.EVENT_BUS.register(proxy);
        MinecraftForge.EVENT_BUS.register(new BackpackUnequipHandler());
	}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(TileBackpack.class, "charset:backpack");

        proxy.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new StorageGuiHandler());
    }
}
