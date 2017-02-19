package pl.asie.charset.audio.transport;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.lib.wires.WireUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        TileEntity tileEntity = getContainer().world().getTileEntity(pos);
        if (tileEntity != null) {
            if (tileEntity.hasCapability(Capabilities.AUDIO_SOURCE, direction)
                    || tileEntity.hasCapability(Capabilities.AUDIO_RECEIVER, direction)) {
                return true;
            }
        }

        return false;
    }

    // TODO: Add multipart-friendly CapabilityHelper
    private boolean receive(BlockPos pos, EnumFacing facing, AudioPacket packet) {
        TileEntity tileEntity = getContainer().world().getTileEntity(pos);
        if (tileEntity != null) {
            IAudioReceiver receiver = CapabilityHelper.get(Capabilities.AUDIO_RECEIVER, tileEntity, facing);
            if (receiver != null) {
                return receiver.receive(packet);
            }
        }

        return false;
    }

    private boolean receive(BlockPos pos, WireFace face, EnumFacing facing, AudioPacket packet) {
        Wire wire = WireUtils.getWire(getContainer().world(), pos, face);
        if (wire != null && wire.hasCapability(Capabilities.AUDIO_RECEIVER, facing)) {
            return wire.getCapability(Capabilities.AUDIO_RECEIVER, facing).receive(packet);
        } else {
            return false;
        }
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

        if (getLocation() != WireFace.CENTER) {
            received |= receive(getContainer().pos().offset(getLocation().facing), getLocation().facing.getOpposite(), packet);
        }

        for (WireFace face : WireFace.VALUES) {
            if (connectsInternal(face)) {
                received |= receive(getContainer().pos(), face, getLocation().facing, packet);
            } else if (face != WireFace.CENTER) {
                if (connectsExternal(face.facing)) {
                    received |= receive(getContainer().pos().offset(face.facing), getLocation(), face.facing.getOpposite(), packet);
                } else if (connectsCorner(face.facing)) {
                    received |= receive(getContainer().pos().offset(face.facing).offset(getLocation().facing), WireFace.get(face.facing.getOpposite()), getLocation().facing.getOpposite(), packet);
                }
            }
        }

        return received;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == Capabilities.AUDIO_RECEIVER) {
            return connects(facing);
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.AUDIO_RECEIVER && connects(facing)) {
            return Capabilities.AUDIO_RECEIVER.cast(this);
        }

        return super.getCapability(capability, facing);
    }
}
