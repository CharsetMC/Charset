/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.capability;

import mcmultipart.api.multipart.MultipartCapabilityHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.carry.CustomCarryHandler;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerProducer;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.api.lib.*;
import pl.asie.charset.api.locks.Lockable;
import pl.asie.charset.api.pipes.IPipeView;
import pl.asie.charset.api.storage.IBarrel;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.capability.audio.AudioReceiverCombiner;
import pl.asie.charset.lib.capability.audio.DefaultAudioReceiver;
import pl.asie.charset.lib.capability.audio.DefaultAudioSource;
import pl.asie.charset.lib.capability.impl.*;
import pl.asie.charset.lib.capability.inventory.DefaultItemInsertionHandler;
import pl.asie.charset.lib.capability.inventory.ItemInsertionHandlerCombiner;
import pl.asie.charset.lib.capability.laser.DummyLaserReceiver;
import pl.asie.charset.lib.capability.laser.LaserReceiverCombiner;
import pl.asie.charset.lib.capability.lib.*;
import pl.asie.charset.lib.capability.mechanical.DefaultMechanicalPowerConsumer;
import pl.asie.charset.lib.capability.mechanical.DefaultMechanicalPowerProducer;
import pl.asie.charset.lib.capability.pipe.DefaultPipeView;
import pl.asie.charset.lib.capability.redstone.*;
import pl.asie.charset.lib.capability.staging.DefaultSignalMeterDataProvider;
import pl.asie.charset.lib.capability.storage.DummyBarrel;
import pl.asie.charset.lib.capability.wrappers.CapabilityWrapperFluidStacks;
import pl.asie.charset.lib.capability.wrappers.CapabilityWrapperInsertionToItemHandler;
import pl.asie.charset.lib.capability.wrappers.CapabilityWrapperInventory;
import pl.asie.charset.api.lib.IDyeableItem;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataProvider;

import javax.annotation.Nullable;

public class Capabilities {
	@CapabilityInject(IAudioSource.class)
	public static Capability<IAudioSource> AUDIO_SOURCE;
	@CapabilityInject(IAudioReceiver.class)
	public static Capability<IAudioReceiver> AUDIO_RECEIVER;

	@CapabilityInject(IAxisRotatable.class)
	public static Capability<IAxisRotatable> AXIS_ROTATABLE;
	@CapabilityInject(IDebuggable.class)
	public static Capability<IDebuggable> DEBUGGABLE;
	@CapabilityInject(IMovable.class)
	public static Capability<IMovable> MOVABLE;

	@CapabilityInject(IDyeableItem.class)
	public static Capability<IDyeableItem> DYEABLE_ITEM;

	@CapabilityInject(IItemInsertionHandler.class)
	public static Capability<IItemInsertionHandler> ITEM_INSERTION_HANDLER;
	@CapabilityInject(IPipeView.class)
	public static Capability<IPipeView> PIPE_VIEW;

	@CapabilityInject(IMechanicalPowerProducer.class)
	public static Capability<IMechanicalPowerProducer> MECHANICAL_PRODUCER;
	@CapabilityInject(IMechanicalPowerConsumer.class)
	public static Capability<IMechanicalPowerConsumer> MECHANICAL_CONSUMER;

	@CapabilityInject(IBundledEmitter.class)
	public static Capability<IBundledEmitter> BUNDLED_EMITTER;
	@CapabilityInject(IBundledReceiver.class)
	public static Capability<IBundledReceiver> BUNDLED_RECEIVER;
	@CapabilityInject(IRedstoneEmitter.class)
	public static Capability<IRedstoneEmitter> REDSTONE_EMITTER;
	@CapabilityInject(IRedstoneReceiver.class)
	public static Capability<IRedstoneReceiver> REDSTONE_RECEIVER;

	@CapabilityInject(ISignalMeterDataProvider.class)
	public static Capability<ISignalMeterDataProvider> SIGNAL_METER_DATA_PROVIDER;

	@CapabilityInject(IBarrel.class)
	public static Capability<IBarrel> BARREL;
	@CapabilityInject(Lockable.class)
	public static Capability<Lockable> LOCKABLE;

	@CapabilityInject(IMultiblockStructure.class)
	public static Capability<IMultiblockStructure> MULTIBLOCK_STRUCTURE;
	@CapabilityInject(CustomCarryHandler.Provider.class)
	public static Capability<CustomCarryHandler.Provider> CUSTOM_CARRY_PROVIDER;

	@CapabilityInject(ILaserReceiver.class)
	public static Capability<ILaserReceiver> LASER_RECEIVER;

	public static Capability.IStorage<Lockable> LOCKABLE_STORAGE = new Capability.IStorage<Lockable>() {
		@Nullable
		@Override
		public NBTBase writeNBT(Capability<Lockable> capability, Lockable instance, EnumFacing side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<Lockable> capability, Lockable instance, EnumFacing side, NBTBase nbt) {
			if (nbt instanceof NBTTagCompound) {
				instance.deserializeNBT((NBTTagCompound) nbt);
			}
		}
	};

	private static final ResourceLocation MULTIBLOCK_STRUCTURE_LOC = new ResourceLocation("charset:multiblock_structure");
	private static CapabilityProviderFactory<IMultiblockStructure> multiblockStructureFactory;

	public static void preInit() {
		CapabilityManager.INSTANCE.register(IAudioSource.class, DummyCapabilityStorage.get(), DefaultAudioSource::new);
		CapabilityManager.INSTANCE.register(IAudioReceiver.class, DummyCapabilityStorage.get(), DefaultAudioReceiver::new);

		CapabilityManager.INSTANCE.register(IAxisRotatable.class, DummyCapabilityStorage.get(), DefaultAxisRotatable::new);
		CapabilityManager.INSTANCE.register(IDebuggable.class, DummyCapabilityStorage.get(), DefaultDebuggable::new);
		CapabilityManager.INSTANCE.register(IMovable.class, DummyCapabilityStorage.get(), DefaultMovable::new);

		CapabilityManager.INSTANCE.register(IDyeableItem.class, new DyeableItemStorage(), DyeableItem::new);

		CapabilityManager.INSTANCE.register(IItemInsertionHandler.class, DummyCapabilityStorage.get(), DefaultItemInsertionHandler::new);
		CapabilityManager.INSTANCE.register(IPipeView.class, DummyCapabilityStorage.get(), DefaultPipeView::new);

		CapabilityManager.INSTANCE.register(IMechanicalPowerProducer.class, DummyCapabilityStorage.get(), DefaultMechanicalPowerProducer::new);
		CapabilityManager.INSTANCE.register(IMechanicalPowerConsumer.class, DummyCapabilityStorage.get(), DefaultMechanicalPowerConsumer::new);

		CapabilityManager.INSTANCE.register(IBundledEmitter.class, new DefaultBundledEmitterStorage(), DefaultBundledEmitter::new);
		CapabilityManager.INSTANCE.register(IRedstoneEmitter.class, new DefaultRedstoneEmitterStorage(), DefaultRedstoneEmitter::new);
		CapabilityManager.INSTANCE.register(IBundledReceiver.class, DummyCapabilityStorage.get(), DummyRedstoneReceiver::new);
		CapabilityManager.INSTANCE.register(IRedstoneReceiver.class, DummyCapabilityStorage.get(), DummyRedstoneReceiver::new);

		CapabilityManager.INSTANCE.register(ISignalMeterDataProvider.class, DummyCapabilityStorage.get(), DefaultSignalMeterDataProvider::new);

		CapabilityManager.INSTANCE.register(IBarrel.class, DummyCapabilityStorage.get(), DummyBarrel::new);
		CapabilityManager.INSTANCE.register(Lockable.class, LOCKABLE_STORAGE, Lockable::new);
		CapabilityManager.INSTANCE.register(IMultiblockStructure.class, DummyCapabilityStorage.get(), DefaultMultiblockStructure::new);

		CapabilityManager.INSTANCE.register(ILaserReceiver.class, DummyCapabilityStorage.get(), DummyLaserReceiver::new);

		CapabilityManager.INSTANCE.register(CustomCarryHandler.Provider.class, DummyCapabilityStorage.get(), () -> handler -> new CustomCarryHandler(handler));

		MinecraftForge.EVENT_BUS.register(new Capabilities());

		multiblockStructureFactory = new CapabilityProviderFactory<>(Capabilities.MULTIBLOCK_STRUCTURE, DummyCapabilityStorage.get());
 	}

 	public static void init() {
		if (Loader.isModLoaded("mcmultipart")) {
			initMultiplePants();
		}

		CapabilityHelper.registerBlockProvider(Capabilities.CUSTOM_CARRY_PROVIDER, Blocks.MOB_SPAWNER, (a, b, c, d) -> CustomCarryHandlerMobSpawner::new);

		for (Block block : Block.REGISTRY) {
			if (block instanceof BlockDoor
					&& block.getBlockState().getProperties().contains(BlockDoor.HALF)) {
				CapabilityHelper.registerBlockProvider(Capabilities.MULTIBLOCK_STRUCTURE, block, MultiblockStructureDoor::new);
			}

			if (block instanceof BlockCactus) {
				CapabilityHelper.registerBlockProvider(Capabilities.CUSTOM_CARRY_PROVIDER, block, (a, b, c, d) -> CustomCarryHandlerCactus::new);
			}

			if (block instanceof BlockChest) {
				CapabilityHelper.registerBlockProvider(Capabilities.CUSTOM_CARRY_PROVIDER, block, (a, b, c, d) -> CustomCarryHandlerChest::new);
			}
		}
	}

	@Optional.Method(modid = "mcmultipart")
	private static void initMultiplePants() {
		MultipartCapabilityHelper.registerCapabilityJoiner(AUDIO_RECEIVER, new AudioReceiverCombiner());
		MultipartCapabilityHelper.registerCapabilityJoiner(BUNDLED_EMITTER, new BundledEmitterCombiner());
		MultipartCapabilityHelper.registerCapabilityJoiner(REDSTONE_EMITTER, new RedstoneEmitterCombiner());
		MultipartCapabilityHelper.registerCapabilityJoiner(BUNDLED_RECEIVER, new BundledReceiverCombiner());
		MultipartCapabilityHelper.registerCapabilityJoiner(REDSTONE_RECEIVER, new RedstoneReceiverCombiner());

		MultipartCapabilityHelper.registerCapabilityJoiner(AXIS_ROTATABLE, new AxisRotatableCombiner());
		MultipartCapabilityHelper.registerCapabilityJoiner(DEBUGGABLE, new DebuggableCombiner());
		MultipartCapabilityHelper.registerCapabilityJoiner(ITEM_INSERTION_HANDLER, new ItemInsertionHandlerCombiner());

		MultipartCapabilityHelper.registerCapabilityJoiner(LASER_RECEIVER, new LaserReceiverCombiner());
	}

	public static void registerVanillaWrappers() {
		CapabilityHelper.registerWrapper(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new CapabilityWrapperInventory());
		CapabilityHelper.registerWrapper(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, new CapabilityWrapperFluidStacks());
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, new CapabilityWrapperInsertionToItemHandler());
	}

	@SubscribeEvent
	public void onAttachCapabilityTile(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof TileEntityChest) {
			event.addCapability(MULTIBLOCK_STRUCTURE_LOC, multiblockStructureFactory.create(new MultiblockStructureChest((TileEntityChest) event.getObject())));
		} else if (event.getObject() instanceof TileEntityBed) {
			event.addCapability(MULTIBLOCK_STRUCTURE_LOC, multiblockStructureFactory.create(new MultiblockStructureBed((TileEntityBed) event.getObject())));
		}
	}

}
