package pl.asie.charset.wires.logic;

import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireType;
import pl.asie.charset.wires.internal.WireLocation;

public class WireInsulated extends WireNormal {
	public WireInsulated(WireType type, WireLocation location, TileWireContainer container) {
		super(type, location, container);
	}

	@Override
	protected int getSignalLevel(TileWireContainer container, WireLocation location) {
		return container.getInsulatedSignalLevel(location, type.color());
	}
}
