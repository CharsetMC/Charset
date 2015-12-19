package pl.asie.charset.api.wires;

public interface IWire {
	WireType getWireType(WireFace location);
	int getInsulatedColor(WireFace location);
}
