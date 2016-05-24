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

import net.minecraftforge.fmp.multipart.IMultipart;
import pl.asie.charset.lib.network.PacketPart;

public class PacketDriveRecord extends PacketPart {
	private byte[] data;
	private int totalLength;
	private boolean isLast;

	public PacketDriveRecord() {
		super();
	}

	public PacketDriveRecord(IMultipart part, byte[] data, int totalLength, boolean isLast) {
		super(part);
		this.totalLength = totalLength;
		this.isLast = isLast;
		this.data = data;
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);

		buf.writeInt(totalLength);
		buf.writeBoolean(isLast);
		buf.writeShort(data.length);
		buf.writeBytes(data);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);

		totalLength = buf.readInt();
		isLast = buf.readBoolean();
		int len = buf.readShort();
		if (len > 0) {
			final byte[] in = new byte[len];
			buf.readBytes(in);

			if (part instanceof PartTapeDrive) {
				if (!getThreadListener(handler).isCallingFromMinecraftThread()) {
					getThreadListener(handler).addScheduledTask(new Runnable() {
						@Override
						public void run() {
							((PartTapeDrive) part).writeData(in, isLast, totalLength);
						}
					});
				} else {
					((PartTapeDrive) part).writeData(in, isLast, totalLength);
				}
			}
		}
	}
}
