package pl.asie.charset.audio.transport;

import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.utils.CapabilityUtils;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.lib.wires.PartWire;
import pl.asie.charset.wires.WireUtils;

import java.util.HashSet;
import java.util.Set;

public class PartAudioCable extends PartWire implements IAudioReceiver {
    private final Set<AudioPacket> receivedPackets = new HashSet<>();

    @Override
    public void update() {
        receivedPackets.clear();
        super.update();
    }

    @Override
    protected void logicUpdate() {

    }

    @Override
    public String getDisplayName() {
        return I18n.translateToLocal("tile.charset.audioCable.name");
    }

    @Override
    public boolean calculateConnectionNonWire(BlockPos pos, EnumFacing direction) {
        if (MultipartUtils.hasCapability(Capabilities.AUDIO_SOURCE, getWorld(), pos, WireUtils.getSlotForFace(location), direction)) {
            return true;
        }

        if (MultipartUtils.hasCapability(Capabilities.AUDIO_RECEIVER, getWorld(), pos, WireUtils.getSlotForFace(location), direction)) {
            return true;
        }

        return false;
    }

    private boolean receive(BlockPos pos, EnumFacing facing, AudioPacket packet) {
        if (CapabilityUtils.hasCapability(getWorld(), pos, Capabilities.AUDIO_RECEIVER, facing, true, false)) {
            return CapabilityUtils.getCapability(getWorld(), pos, Capabilities.AUDIO_RECEIVER, facing, true, false).receive(packet);
        } else {
            return false;
        }
    }

    private boolean receive(BlockPos pos, WireFace face, EnumFacing facing, AudioPacket packet) {
        PartSlot slot = WireUtils.getSlotForFace(face);
        if (MultipartUtils.hasCapability(Capabilities.AUDIO_RECEIVER, getWorld(), pos, slot, facing)) {
            return MultipartUtils.getCapability(Capabilities.AUDIO_RECEIVER, getWorld(), pos, slot, facing).receive(packet);
        } else {
            return false;
        }
    }

    @Override
    public boolean receive(AudioPacket packet) {
        if (receivedPackets.contains(packet)) {
            return false;
        }

        receivedPackets.add(packet);
        boolean received = false;

        if (location != WireFace.CENTER) {
            received |= receive(getPos().offset(location.facing), location.facing.getOpposite(), packet);
        }

        for (WireFace face : WireFace.VALUES) {
            if (connectsInternal(face)) {
                received |= receive(getPos(), face, location.facing, packet);
            } else if (face != WireFace.CENTER) {
                if (connectsExternal(face.facing)) {
                    received |= receive(getPos().offset(face.facing), location, face.facing.getOpposite(), packet);
                } else if (connectsCorner(face.facing)) {
                    received |= receive(getPos().offset(face.facing).offset(location.facing), WireFace.get(face.facing.getOpposite()), location.facing.getOpposite(), packet);
                }
            }
        }

        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, PartSlot slot, EnumFacing facing) {
        if (capability == Capabilities.AUDIO_RECEIVER) {
            return slot == WireUtils.getSlotForFace(location) ? (facing == null || connects(facing)) : false;
        }

        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, PartSlot slot, EnumFacing facing) {
        if (hasCapability(capability, slot, facing)) {
            return capability == Capabilities.AUDIO_RECEIVER ? Capabilities.AUDIO_RECEIVER.cast(this) : null;
        }

        return null;
    }
}
