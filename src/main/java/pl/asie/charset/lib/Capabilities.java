package pl.asie.charset.lib;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import mcmultipart.capabilities.CapabilityWrapperRegistry;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.capability.BundledEmitterWrapper;
import pl.asie.charset.lib.capability.BundledReceiverWrapper;
import pl.asie.charset.lib.capability.DefaultBundledEmitter;
import pl.asie.charset.lib.capability.DefaultBundledEmitterStorage;
import pl.asie.charset.lib.capability.DefaultRedstoneEmitter;
import pl.asie.charset.lib.capability.DefaultRedstoneEmitterStorage;
import pl.asie.charset.lib.capability.DummyRedstoneReceiver;
import pl.asie.charset.lib.capability.RedstoneEmitterWrapper;
import pl.asie.charset.lib.capability.RedstoneReceiverWrapper;

public class Capabilities {
	@CapabilityInject(IBundledEmitter.class)
	public static Capability<IBundledEmitter> BUNDLED_EMITTER;
	@CapabilityInject(IBundledReceiver.class)
	public static Capability<IBundledReceiver> BUNDLED_RECEIVER;
	@CapabilityInject(IRedstoneEmitter.class)
	public static Capability<IRedstoneEmitter> REDSTONE_EMITTER;
	@CapabilityInject(IRedstoneReceiver.class)
	public static Capability<IRedstoneReceiver> REDSTONE_RECEIVER;

	public static void init() {
		CapabilityManager.INSTANCE.register(IBundledEmitter.class, new DefaultBundledEmitterStorage(), DefaultBundledEmitter.class);
		CapabilityManager.INSTANCE.register(IRedstoneEmitter.class, new DefaultRedstoneEmitterStorage(), DefaultRedstoneEmitter.class);

		CapabilityManager.INSTANCE.register(IBundledReceiver.class, new Capability.IStorage<IBundledReceiver>() {
			@Override
			public NBTBase writeNBT(Capability<IBundledReceiver> capability, IBundledReceiver instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IBundledReceiver> capability, IBundledReceiver instance, EnumFacing side, NBTBase nbt) {

			}
		}, DummyRedstoneReceiver.class);

		CapabilityManager.INSTANCE.register(IRedstoneReceiver.class, new Capability.IStorage<IRedstoneReceiver>() {
			@Override
			public NBTBase writeNBT(Capability<IRedstoneReceiver> capability, IRedstoneReceiver instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IRedstoneReceiver> capability, IRedstoneReceiver instance, EnumFacing side, NBTBase nbt) {

			}
		}, DummyRedstoneReceiver.class);

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
