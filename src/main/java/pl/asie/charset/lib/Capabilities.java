package pl.asie.charset.lib;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import mcmultipart.capabilities.CapabilityWrapperRegistry;
import pl.asie.charset.api.audio.IAudioSink;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.capability.*;

public class Capabilities {
	@CapabilityInject(IAudioSource.class)
	public static Capability<IAudioSource> AUDIO_SOURCE;
	@CapabilityInject(IAudioSink.class)
	public static Capability<IAudioSink> AUDIO_SINK;

	@CapabilityInject(IBundledEmitter.class)
	public static Capability<IBundledEmitter> BUNDLED_EMITTER;
	@CapabilityInject(IBundledReceiver.class)
	public static Capability<IBundledReceiver> BUNDLED_RECEIVER;
	@CapabilityInject(IRedstoneEmitter.class)
	public static Capability<IRedstoneEmitter> REDSTONE_EMITTER;
	@CapabilityInject(IRedstoneReceiver.class)
	public static Capability<IRedstoneReceiver> REDSTONE_RECEIVER;

	public static void init() {
		CapabilityManager.INSTANCE.register(IAudioSource.class, new NullCapabilityStorage<IAudioSource>(), DefaultAudioSource.class);
		CapabilityManager.INSTANCE.register(IAudioSink.class, new NullCapabilityStorage<IAudioSink>(), DefaultAudioSink.class);

		CapabilityManager.INSTANCE.register(IBundledEmitter.class, new DefaultBundledEmitterStorage(), DefaultBundledEmitter.class);
		CapabilityManager.INSTANCE.register(IRedstoneEmitter.class, new DefaultRedstoneEmitterStorage(), DefaultRedstoneEmitter.class);

		CapabilityManager.INSTANCE.register(IBundledReceiver.class, new NullCapabilityStorage<IBundledReceiver>(), DummyRedstoneReceiver.class);

		CapabilityManager.INSTANCE.register(IRedstoneReceiver.class, new NullCapabilityStorage<IRedstoneReceiver>(), DummyRedstoneReceiver.class);

		if (Loader.isModLoaded("mcmultipart")) {
			initMultiplePants();
		}
 	}

	@Optional.Method(modid = "mcmultipart")
	private static void initMultiplePants() {
		CapabilityWrapperRegistry.registerCapabilityWrapper(new BundledEmitterWrapper());
		CapabilityWrapperRegistry.registerCapabilityWrapper(new RedstoneEmitterWrapper());
		CapabilityWrapperRegistry.registerCapabilityWrapper(new BundledReceiverWrapper());
		CapabilityWrapperRegistry.registerCapabilityWrapper(new RedstoneReceiverWrapper());
	}
}
