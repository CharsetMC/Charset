package pl.asie.charset.lib.audio;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;

public final class AudioUtils {
    private AudioUtils() {

    }

    public static int start() {
        return AudioStreamManager.INSTANCE.create();
    }

    public static void stop(int id) {
        ModCharsetLib.packet.sendToAll(new PacketAudioStop(id));
        AudioStreamManager.INSTANCE.remove(id);
    }
}
