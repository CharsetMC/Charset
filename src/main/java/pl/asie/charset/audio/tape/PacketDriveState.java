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

package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import net.minecraft.server.MinecraftServer;

import mcmultipart.multipart.IMultipart;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketPart;

public class PacketDriveState extends PacketPart {
	private State state;

	public PacketDriveState() {
		super();
	}

	public PacketDriveState(IMultipart tile, State state) {
		super(tile);
		this.state = state;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		final State newState = State.values()[buf.readUnsignedByte()];

		if (part instanceof PartTapeDrive) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					((PartTapeDrive) part).setState(newState);
				}
			};

			if (part.getWorld().isRemote) {
				if (ModCharsetLib.proxy.isClientThread()) {
					runnable.run();
				} else {
					ModCharsetLib.proxy.addScheduledClientTask(runnable);
				}
			} else {
				if (!getThreadListener(handler).isCallingFromMinecraftThread()) {
					getThreadListener(handler).addScheduledTask(runnable);
				} else {
					runnable.run();
				}
			}
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeByte(state.ordinal());
	}
}
