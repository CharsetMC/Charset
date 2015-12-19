package pl.asie.charset.wires.logic;

import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireKind;
import pl.asie.charset.api.wires.WireFace;

public class WireInsulated extends WireNormal {
	public WireInsulated(WireKind type, WireFace location, TileWireContainer container) {
		super(type, location, container);
	}

	@Override
	protected int getSignalLevel(TileWireContainer container, WireFace location) {
		return container.getInsulatedSignalLevel(location, type.color());
	}
}
