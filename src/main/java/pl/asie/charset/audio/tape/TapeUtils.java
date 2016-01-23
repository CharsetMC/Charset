package pl.asie.charset.audio.tape;

import java.util.List;

import net.minecraft.util.EnumChatFormatting;

public final class TapeUtils {
	private TapeUtils() {

	}

	public static void addTooltip(List<String> tooltip, int mins, int secs) {
		if (mins != 0) {
			tooltip.add(EnumChatFormatting.GRAY + "" + mins + " minutes " + (secs != 0 ? secs + " seconds" : ""));
		} else {
			tooltip.add(EnumChatFormatting.GRAY + "" + secs + " seconds");
		}
	}
}
