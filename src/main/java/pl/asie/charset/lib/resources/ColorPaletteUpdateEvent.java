package pl.asie.charset.lib.resources;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ColorPaletteUpdateEvent extends Event {
	private final ColorPaletteParser parser;

	public ColorPaletteUpdateEvent(ColorPaletteParser parser) {
		this.parser = parser;
	}

	public ColorPaletteParser getParser() {
		return parser;
	}
}
