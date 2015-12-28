package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

public class PartGateAND extends PartGate {
    @Override
    public State getLayerState(int id) {
        switch (id) {
            case 0:
                return State.input(getOutputClient());
            case 1:
                if (!isSideOpen(EnumFacing.WEST)) {
                    return State.DISABLED;
                }
                return State.input(getInputInside(0));
            case 2:
                if (!isSideOpen(EnumFacing.EAST)) {
                    return State.DISABLED;
                }
                return State.input(getInputInside(2));
            case 3:
                if (!isSideOpen(EnumFacing.SOUTH)) {
                    return State.DISABLED;
                }
                return State.input(getInputInside(1));
        }
        return State.OFF;
    }

    @Override
    public State getTorchState(int id) {
        switch (id) {
            case 0:
            case 1:
            case 2:
                return State.input(getInputInside(id)).invert();
            case 3:
                return State.input(getOutputClient()).invert();
        }
        return State.ON;
    }

    @Override
    public int getOutputLevel() {
        for (int i = 0; i <= 2; i++) {
            if (isSideOpen(INPUT_SIDES[i]) && getInputInside(i) == 0) {
                return 0;
            }
        }
        return 15;
    }
}
