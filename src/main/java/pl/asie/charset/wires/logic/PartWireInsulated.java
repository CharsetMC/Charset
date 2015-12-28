package pl.asie.charset.wires.logic;

import net.minecraft.util.EnumFacing;

import mcmultipart.multipart.IMultipartContainer;
import pl.asie.charset.api.wires.IInsulatedWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.wires.WireUtils;

public class PartWireInsulated extends PartWireNormal implements IInsulatedWire {
	@Override
    protected int getRedstoneLevel(IMultipartContainer container, WireFace location) {
        return WireUtils.getInsulatedWireLevel(container, location, type.color());
    }

    @Override
    protected void onSignalChanged(int color) {
        if (color == type.color() || color == -1) {
            propagate(color);
        }
    }

    @Override
    public int getWireColor() {
        return type.color();
    }

    @Override
    public int getRedstoneSignal(WireFace face, EnumFacing toDirection) {
        return face == location && connects(toDirection) ? getRedstoneLevel() : 0;
    }

    @Override
    public void onRedstoneInputChanged(EnumFacing face) {
        if (connects(face)) {
            schedulePropagationUpdate();
        }
    }
}
