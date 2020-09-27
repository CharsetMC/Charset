/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

import javax.annotation.Nullable;
import java.util.*;

public class TileWirelessReceiver extends TileBase implements IAudioSource, IWirelessAudioReceiver, ITickable {
	public Map<UUID, Deque<AudioData>> packetQueue = new HashMap<>();

	@Override
	public void receiveWireless(UUID senderId, AudioData packet) {
		packetQueue.computeIfAbsent(senderId, (i) -> new ArrayDeque<>()).addLast(packet);
	}

	@Override
	public void update() {
		super.update();

		Iterator<Deque<AudioData>> dequeIterator = packetQueue.values().iterator();
		while (dequeIterator.hasNext()) {
			Deque<AudioData> deque = dequeIterator.next();
			if (deque.isEmpty()) {
				dequeIterator.remove();
			} else {
				AudioData data = deque.removeFirst();
				AudioPacket packetSent = new AudioPacket(data, 1.0f);

				for (EnumFacing facing : EnumFacing.VALUES) {
					BlockPos nPos = pos.offset(facing);
					IAudioReceiver receiver = CapabilityHelper.get(getWorld(), nPos, Capabilities.AUDIO_RECEIVER, facing.getOpposite(),
							false, true, false);
					if (receiver != null) {
						receiver.receive(packetSent);
					}
				}
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == Capabilities.AUDIO_SOURCE || capability == CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == Capabilities.AUDIO_SOURCE || capability == CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER) {
			//noinspection unchecked
			return (T) (this);
		} else {
			return super.getCapability(capability, facing);
		}
	}
}
