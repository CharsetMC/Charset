package pl.asie.charset.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import mcmultipart.multipart.MultipartRegistry;
import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.note.BlockIronNote;
import pl.asie.charset.audio.note.PacketNoteParticle;
import pl.asie.charset.audio.note.TileIronNote;
import pl.asie.charset.audio.storage.DataStorageImpl;
import pl.asie.charset.audio.storage.DataStorageManager;
import pl.asie.charset.audio.storage.DataStorageStorage;
import pl.asie.charset.audio.tape.ItemPartTapeDrive;
import pl.asie.charset.audio.tape.ItemTape;
import pl.asie.charset.audio.tape.PacketDriveAudio;
import pl.asie.charset.audio.tape.PacketDriveCounter;
import pl.asie.charset.audio.tape.PacketDriveRecord;
import pl.asie.charset.audio.tape.PacketDriveState;
import pl.asie.charset.audio.tape.PacketDriveStop;
import pl.asie.charset.audio.tape.PartTapeDrive;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;

@Mod(modid = ModCharsetAudio.MODID, name = ModCharsetAudio.NAME, version = ModCharsetAudio.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetAudio {
	public static final String MODID = "CharsetAudio";
	public static final String NAME = "♫";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.audio.ProxyClient", serverSide = "pl.asie.charset.audio.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetAudio instance;

	@CapabilityInject(IDataStorage.class)
	public static Capability<IDataStorage> CAP_STORAGE;

	public static Logger logger;

	public static PacketRegistry packet;
	public static DataStorageManager storage;

	public static BlockIronNote ironNoteBlock;
	public static ItemPartTapeDrive partTapeDriveItem;
	public static ItemTape tapeItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetAudio.MODID);

		if (ModCharsetLib.INDEV) {
			partTapeDriveItem = new ItemPartTapeDrive();
			GameRegistry.registerItem(partTapeDriveItem, "tapeDrive");

			tapeItem = new ItemTape();
			GameRegistry.registerItem(tapeItem, "tape");

			MultipartRegistry.registerPart(PartTapeDrive.class, "charsetaudio:tapedrive");

			ModCharsetLib.proxy.registerItemModel(partTapeDriveItem, 0, "charsetaudio:tapedrive");
			ModCharsetLib.proxy.registerItemModel(tapeItem, 0, "charsetaudio:tape");

			CapabilityManager.INSTANCE.register(IDataStorage.class, new DataStorageStorage(), DataStorageImpl.class);
		}

		ironNoteBlock = new BlockIronNote();
		GameRegistry.registerBlock(ironNoteBlock, "ironnoteblock");
		ModCharsetLib.proxy.registerItemModel(ironNoteBlock, 0, "charsetaudio:ironnoteblock");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet = new PacketRegistry(ModCharsetAudio.MODID);
		packet.registerPacket(0x01, PacketNoteParticle.class);

		packet.registerPacket(0x10, PacketDriveState.class);
		packet.registerPacket(0x11, PacketDriveAudio.class);
		packet.registerPacket(0x12, PacketDriveStop.class);
		packet.registerPacket(0x13, PacketDriveRecord.class);
		packet.registerPacket(0x14, PacketDriveCounter.class);

		GameRegistry.registerTileEntity(TileIronNote.class, "charset:ironnoteblock");

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ironNoteBlock), "iii", "iNi", "iii", 'i', "ingotIron", 'N', Blocks.noteblock));

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerAudio());

		proxy.init();
	}

	@Mod.EventHandler
	public void serverStart(FMLServerAboutToStartEvent event) {
		if (ModCharsetLib.INDEV) {
			storage = new DataStorageManager();
		}
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		if (ModCharsetLib.INDEV) {
			storage = null;
			proxy.onServerStop();
		}
	}
}
