package pl.asie.charset.lib.audio;

import mcmultipart.multipart.IMultipart;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSink;

public class AudioSinkPart implements IAudioSink {
    private final IMultipart part;

    public AudioSinkPart(IMultipart part) {
        this.part = part;
    }

    @Override
    public World getWorld() {
        return part.getWorld();
    }

    @Override
    public Vec3d getPos() {
        return new Vec3d(part.getPos());
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
