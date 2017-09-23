package pl.asie.charset.lib.config;

import net.minecraftforge.fml.common.event.FMLEvent;

public class CharsetLoadConfigEvent extends FMLEvent {
	private final boolean firstTime;

	public CharsetLoadConfigEvent(boolean firstTime) {
		this.firstTime = firstTime;
	}

	public boolean isFirstTime() {
		return firstTime;
	}
}
