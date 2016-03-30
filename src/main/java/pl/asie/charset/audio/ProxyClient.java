package pl.asie.charset.audio;

import net.minecraft.client.Minecraft;
import pl.asie.charset.audio.manager.AudioStreamManager;
import pl.asie.charset.audio.tape.ItemTape;

public class ProxyClient extends ProxyCommon {
	public static AudioStreamManager stream;

	@Override
	public void init() {
		stream = new AudioStreamManager();
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemTape.Color(ModCharsetAudio.tapeItem), ModCharsetAudio.tapeItem);
	}

	@Override
	public void onServerStop() {
		stream.removeAll();
	}
}
