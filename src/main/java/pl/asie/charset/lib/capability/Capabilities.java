/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.capability;

import mcmultipart.api.multipart.MultipartCapabilityHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.api.lib.IMovable;
import pl.asie.charset.api.pipes.IPipeView;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.capability.audio.AudioReceiverWrapper;
import pl.asie.charset.lib.capability.audio.DefaultAudioReceiver;
import pl.asie.charset.lib.capability.audio.DefaultAudioSource;
import pl.asie.charset.lib.capability.inventory.DefaultItemInsertionHandler;
import pl.asie.charset.lib.capability.inventory.ItemInsertionHandlerWrapper;
import pl.asie.charset.lib.capability.lib.*;
import pl.asie.charset.lib.capability.pipe.DefaultPipeView;
import pl.asie.charset.lib.capability.wrappers.CapabilityWrapperFluidStacks;
import pl.asie.charset.lib.capability.wrappers.CapabilityWrapperInsertionToItemHandler;
import pl.asie.charset.lib.capability.wrappers.CapabilityWrapperInventory;
import pl.asie.charset.lib.capability.redstone.*;

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

	@CapabilityInject(IItemInsertionHandler.class)
	public static Capability<IItemInsertionHandler> ITEM_INSERTION_HANDLER;
	@CapabilityInject(IPipeView.class)
	public static Capability<IPipeView> PIPE_VIEW;

	@CapabilityInject(IBundledEmitter.class)
	public static Capability<IBundledEmitter> BUNDLED_EMITTER;
	@CapabilityInject(IBundledReceiver.class)
	public static Capability<IBundledReceiver> BUNDLED_RECEIVER;
	@CapabilityInject(IRedstoneEmitter.class)
	public static Capability<IRedstoneEmitter> REDSTONE_EMITTER;
	@CapabilityInject(IRedstoneReceiver.class)
	public static Capability<IRedstoneReceiver> REDSTONE_RECEIVER;

	public static void preInit() {
		CapabilityManager.INSTANCE.register(IAudioSource.class, new DummyCapabilityStorage<>(), DefaultAudioSource::new);
		CapabilityManager.INSTANCE.register(IAudioReceiver.class, new DummyCapabilityStorage<>(), DefaultAudioReceiver::new);

		CapabilityManager.INSTANCE.register(IAxisRotatable.class, new DummyCapabilityStorage<>(), DefaultAxisRotatable::new);
		CapabilityManager.INSTANCE.register(IDebuggable.class, new DummyCapabilityStorage<>(), DefaultDebuggable::new);
		CapabilityManager.INSTANCE.register(IMovable.class, new DummyCapabilityStorage<>(), DefaultMovable::new);

		CapabilityManager.INSTANCE.register(IItemInsertionHandler.class, new DummyCapabilityStorage<>(), DefaultItemInsertionHandler::new);
		CapabilityManager.INSTANCE.register(IPipeView.class, new DummyCapabilityStorage<>(), DefaultPipeView::new);

		CapabilityManager.INSTANCE.register(IBundledEmitter.class, new DefaultBundledEmitterStorage(), DefaultBundledEmitter::new);
		CapabilityManager.INSTANCE.register(IRedstoneEmitter.class, new DefaultRedstoneEmitterStorage(), DefaultRedstoneEmitter::new);
		CapabilityManager.INSTANCE.register(IBundledReceiver.class, new DummyCapabilityStorage<>(), DummyRedstoneReceiver::new);
		CapabilityManager.INSTANCE.register(IRedstoneReceiver.class, new DummyCapabilityStorage<>(), DummyRedstoneReceiver::new);
 	}

 	public static void init() {
		if (Loader.isModLoaded("mcmultipart")) {
			initMultiplePants();
		}
	}

	@Optional.Method(modid = "mcmultipart")
	private static void initMultiplePants() {
		MultipartCapabilityHelper.registerCapabilityJoiner(AUDIO_RECEIVER, new AudioReceiverWrapper());
		MultipartCapabilityHelper.registerCapabilityJoiner(BUNDLED_EMITTER, new BundledEmitterWrapper());
		MultipartCapabilityHelper.registerCapabilityJoiner(REDSTONE_EMITTER, new RedstoneEmitterWrapper());
		MultipartCapabilityHelper.registerCapabilityJoiner(BUNDLED_RECEIVER, new BundledReceiverWrapper());
		MultipartCapabilityHelper.registerCapabilityJoiner(REDSTONE_RECEIVER, new RedstoneReceiverWrapper());

		MultipartCapabilityHelper.registerCapabilityJoiner(AXIS_ROTATABLE, new AxisRotatableWrapper());
		MultipartCapabilityHelper.registerCapabilityJoiner(DEBUGGABLE, new DebuggableWrapper());
		MultipartCapabilityHelper.registerCapabilityJoiner(ITEM_INSERTION_HANDLER, new ItemInsertionHandlerWrapper());
	}

	public static void registerVanillaWrappers() {
		CapabilityHelper.registerWrapper(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new CapabilityWrapperInventory());
		CapabilityHelper.registerWrapper(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, new CapabilityWrapperFluidStacks());
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, new CapabilityWrapperInsertionToItemHandler());
	}
}
