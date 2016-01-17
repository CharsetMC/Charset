package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateXOR extends PartGate {
	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.SOUTH) {
			return Connection.NONE;
		} else {
			return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
		}
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
			case 1:
				return State.input(getInputInside(EnumFacing.WEST));
			case 2:
				return State.input(getInputInside(EnumFacing.EAST));
			case 3:
				return State.bool(getInputInside(EnumFacing.WEST) == 0 && getInputInside(EnumFacing.EAST) == 0);
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputInside(EnumFacing.WEST)).invert();
			case 1:
				return State.input(getInputInside(EnumFacing.EAST)).invert();
			case 2:
				return State.bool(getInputInside(EnumFacing.WEST) == 0 && getInputInside(EnumFacing.EAST) == 0).invert();
		}
		return State.ON;
	}

	@Override
	public byte getOutputInside(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return digiToRs(rsToDigi(getInputInside(EnumFacing.WEST)) ^ rsToDigi(getInputInside(EnumFacing.EAST)));
		} else {
			return 0;
		}
	}
}
