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
