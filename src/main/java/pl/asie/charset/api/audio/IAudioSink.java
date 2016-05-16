package pl.asie.charset.api.audio;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IAudioSink {
    World getWorld();
    Vec3d getPos();
    float getHearingDistance();
    float getVolume();
    boolean receive(AudioPacket packet);
}
