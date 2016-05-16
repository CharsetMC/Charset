package pl.asie.charset.lib.capability;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSink;

// TODO: Make this work?
public class DefaultAudioSink implements IAudioSink {
    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public Vec3d getPos() {
        return null;
    }

    @Override
    public float getHearingDistance() {
        return 32.0f;
    }

    @Override
    public float getVolume() {
        return 1.0f;
    }

    @Override
    public boolean receive(AudioPacket packet) {
        return packet.add(this);
    }
}
