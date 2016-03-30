package pl.asie.charset.audio.tape;

import java.util.List;

import net.minecraft.util.text.TextFormatting;

public final class TapeUtils {
	private TapeUtils() {

	}

	public static void addTooltip(List<String> tooltip, int mins, int secs) {
		if (mins != 0) {
			tooltip.add(TextFormatting.GRAY + "" + mins + " minutes " + (secs != 0 ? secs + " seconds" : ""));
		} else {
			tooltip.add(TextFormatting.GRAY + "" + secs + " seconds");
		}
	}
}
