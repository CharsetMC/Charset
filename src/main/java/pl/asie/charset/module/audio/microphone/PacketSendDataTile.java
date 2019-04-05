/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.audio.microphone;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.lib.network.PacketTile;

import java.util.UUID;

public class PacketSendDataTile extends PacketTile {
	private UUID senderId;
	private AudioData data;

	public PacketSendDataTile() {
		super();
	}

	public PacketSendDataTile(TileEntity tile, UUID senderId, AudioData data) {
		super(tile);
		this.senderId = senderId;
		this.data = data;
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeUniqueId(senderId);
		buf.writeShort(CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).getId(data));
		data.writeData(buf);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		senderId = buf.readUniqueId();
		data = CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).create(buf.readUnsignedShort());
		data.readData(buf);
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);

		if (tile != null && tile.hasCapability(CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER, null)) {
			IWirelessAudioReceiver receiver = tile.getCapability(CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER, null);
			//noinspection ConstantConditions
			receiver.receiveWireless(senderId, data);
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
