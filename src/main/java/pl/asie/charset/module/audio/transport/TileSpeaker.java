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

package pl.asie.charset.module.audio.transport;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.lib.audio.types.AudioSinkBlock;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;

public class TileSpeaker extends TileBase implements IAudioReceiver {
	private AudioSink sink;

	@Override
	public void invalidate(InvalidationType type) {
		super.invalidate(type);
		sink = null;
	}

	@Override
	public void validate() {
		super.validate();
		sink = new AudioSinkBlock(getWorld(), getPos());
	}

	@Override
	public boolean receive(AudioPacket packet) {
		if (sink != null) {
			packet.add(sink);
		}
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == Capabilities.AUDIO_RECEIVER || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == Capabilities.AUDIO_RECEIVER) {
			return Capabilities.AUDIO_RECEIVER.cast(this);
		} else {
			return super.getCapability(capability, facing);
		}
	}
}
