package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateXOR extends PartGate {
    @Override
    public State getLayerState(int id) {
        switch (id) {
            case 0:
                return State.input(getOutputClient());
            case 1:
                return State.input(getInputInside(0));
            case 2:
                return State.input(getInputInside(2));
            case 3:
                return State.bool(getInputInside(0) == 0 && getInputInside(2) == 0);
        }
        return State.OFF;
    }

    @Override
    public State getTorchState(int id) {
        switch (id) {
            case 0:
                return State.input(getInputInside(0)).invert();
            case 1:
                return State.input(getInputInside(2)).invert();
            case 2:
                return State.bool(getInputInside(0) == 0 && getInputInside(2) == 0).invert();
        }
        return State.ON;
    }

    @Override
    public boolean canBlockSide(EnumFacing side) {
        return false;
    }

    @Override
    protected byte getSideMask() {
        return 0b1101;
    }

    @Override
    public int getOutputLevel() {
        return digiToRs(rsToDigi(getInputInside(0)) ^ rsToDigi(getInputInside(2)));
    }
}
