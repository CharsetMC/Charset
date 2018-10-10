
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
