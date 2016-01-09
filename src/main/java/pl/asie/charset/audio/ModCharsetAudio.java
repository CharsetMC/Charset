package pl.asie.charset.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import pl.asie.charset.audio.note.BlockIronNote;
import pl.asie.charset.audio.note.TileIronNote;
import pl.asie.charset.audio.tape.BlockTapeDrive;
import pl.asie.charset.audio.tape.ItemTape;
import pl.asie.charset.audio.tape.StorageManager;
import pl.asie.charset.audio.util.AudioStreamManager;
import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetAudio.MODID, name = ModCharsetAudio.NAME, version = ModCharsetAudio.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetAudio {
	public static final String MODID = "CharsetAudio";
	public static final String NAME = "♫";
	public static final String VERSION = "@VERSION@";

	public static Logger logger;

	@SidedProxy(clientSide = "pl.asie.charset.audio.client.AudioStreamManagerClient", serverSide = "pl.asie.charset.audio.util.AudioStreamManager")
	public static AudioStreamManager audio;
	public static StorageManager storage;

	public static BlockTapeDrive tapeDriveBlock;
    public static BlockIronNote ironNoteBlock;
	public static ItemTape tapeItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetAudio.MODID);

		/* tapeDriveBlock = new BlockTapeDrive();
		GameRegistry.registerBlock(tapeDriveBlock, "tapedrive");

		tapeItem = new ItemTape();
		GameRegistry.registerItem(tapeItem, "tape");

		ModCharsetLib.proxy.registerItemModel(tapeItem, 0, "charsetaudio:tape"); */

        ironNoteBlock = new BlockIronNote();
        GameRegistry.registerBlock(ironNoteBlock, "ironnoteblock");
        ModCharsetLib.proxy.registerItemModel(ironNoteBlock, 0, "charsetaudio:ironnoteblock");
	}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(TileIronNote.class, "charset:ironnoteblock");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ironNoteBlock), "iii", "iNi", "iii", 'i', "ingotIron", 'N', Blocks.noteblock));
    }

	@Mod.EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		storage = new StorageManager();
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		storage = null;
	}
}
