package pl.asie.charset.lib.wires;

import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.blocks.TileBase;

public class TileWire extends TileBase {
    private final Wire[] wires = new Wire[7];

    protected Wire getWire(WireFace face) {
        return wires[face.ordinal()];
    }
}
