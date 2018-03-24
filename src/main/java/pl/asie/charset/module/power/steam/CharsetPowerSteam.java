package pl.asie.charset.module.power.steam;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
		name = "power.steam",
		description = "Steam power system",
		profile = ModuleProfile.INDEV
)
public class CharsetPowerSteam {
	private static final ResourceLocation SCC_LOCATION = new ResourceLocation("charset", "steam_chunk_container");

	@CapabilityInject(SteamChunkContainer.class)
	public static Capability<SteamChunkContainer> chunkContainerCapability;
	private static CapabilityProviderFactory<SteamChunkContainer> factory;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(SteamChunkContainer.class, DummyCapabilityStorage.get(), SteamChunkContainer::new);
	}

	@SubscribeEvent
	public void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event) {
		if (factory == null) {
			factory = new CapabilityProviderFactory<>(chunkContainerCapability);
		}

		event.addCapability(SCC_LOCATION, factory.create(new SteamChunkContainer()));
	}
}
