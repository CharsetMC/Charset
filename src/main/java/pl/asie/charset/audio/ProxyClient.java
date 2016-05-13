package pl.asie.charset.audio;

import net.minecraft.client.Minecraft;
import pl.asie.charset.audio.tape.ItemTape;

public class ProxyClient extends ProxyCommon {

	@Override
	public void init() {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemTape.Color(ModCharsetAudio.tapeItem), ModCharsetAudio.tapeItem);
	}
}
