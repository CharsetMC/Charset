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

package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.network.Packet;

public class PacketAudioStop extends Packet {
	public PacketAudioStop() {
		super();
	}
	private int id;

	protected PacketAudioStop(int id) {
		super();
		this.id = id;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		this.id = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		AudioStreamManager.INSTANCE.remove(id);
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(id);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
