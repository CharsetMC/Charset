package pl.asie.charset.module.audio.transport;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.lib.wires.WireUtils;

import java.util.HashSet;
import java.util.Set;

public class WireAudioCable extends Wire implements IAudioReceiver {
    public WireAudioCable(IWireContainer container, WireProvider factory, WireFace location) {
        super(container, factory, location);
    }

    private final Set<AudioPacket> receivedPackets = new HashSet<>();
    private long lastReceivedTime = -1;

    @Override
    public boolean canConnectBlock(BlockPos pos, EnumFacing direction) {
        return WireUtils.hasCapability(this, pos, Capabilities.AUDIO_SOURCE, direction, true)
                || WireUtils.hasCapability(this, pos, Capabilities.AUDIO_RECEIVER, direction, true);
    }

    @Override
    public boolean receive(AudioPacket packet) {
        if (lastReceivedTime != getContainer().world().getTotalWorldTime()) {
            lastReceivedTime = getContainer().world().getTotalWorldTime();
            receivedPackets.clear();
        }

        if (receivedPackets.contains(packet)) {
            return false;
        }

        receivedPackets.add(packet);

        boolean received = false;
        for (IAudioReceiver receiver : connectedIterator(Capabilities.AUDIO_RECEIVER, true)) {
            received |= receiver.receive(packet);
        }

        return received;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == Capabilities.AUDIO_RECEIVER) {
            return connects(facing) || (getLocation().facing != null && getLocation().facing == facing);
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.AUDIO_RECEIVER && (connects(facing) || (getLocation().facing != null && getLocation().facing == facing))) {
            return Capabilities.AUDIO_RECEIVER.cast(this);
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public String getDisplayName() {
        return "charset.audioCable";
    }
}
