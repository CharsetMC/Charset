package pl.asie.charset.module.audio.microphone;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;

// PRAISE BTM
@CharsetModule(
		name = "audio.microphone",
		description = "Microphones!",
		profile = ModuleProfile.INDEV
)
public class CharsetAudioMicrophone {
	@CapabilityInject(IWirelessAudioReceiver.class)
	public static Capability<IWirelessAudioReceiver> WIRELESS_AUDIO_RECEIVER;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static BlockWirelessReceiver blockWirelessReceiver;
	public static ItemBlock itemWirelessReceiver;
	public static ItemMicrophone itemMicrophone;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(IWirelessAudioReceiver.class, DummyCapabilityStorage.get(), () -> (a, b) -> {});

		blockWirelessReceiver = new BlockWirelessReceiver();
		itemWirelessReceiver = new ItemBlockBase(blockWirelessReceiver);
		itemMicrophone = new ItemMicrophone();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileWirelessReceiver.class, "audio_wireless_receiver");

		packet.registerPacket(0x01, PacketSendDataTile.class);
	}

	@SideOnly(Side.CLIENT)
	@Mod.EventHandler
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new MicrophoneEventHandler());
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockWirelessReceiver, "audio_wireless_receiver");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemWirelessReceiver, "audio_wireless_receiver");
		RegistryUtils.register(event.getRegistry(), itemMicrophone, "audio_microphone");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRegisterModels(ModelRegistryEvent event) {
		// TODO
	}
}
