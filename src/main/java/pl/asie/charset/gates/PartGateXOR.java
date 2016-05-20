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
				return State.input(getValueInside(EnumFacing.NORTH));
			case 1:
				return State.input(getValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getValueInside(EnumFacing.EAST));
			case 3:
				return State.bool(getValueInside(EnumFacing.WEST) == 0 && getValueInside(EnumFacing.EAST) == 0);
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.WEST)).invert();
			case 1:
				return State.input(getValueInside(EnumFacing.EAST)).invert();
			case 2:
				return State.bool(getValueInside(EnumFacing.WEST) == 0 && getValueInside(EnumFacing.EAST) == 0).invert();
		}
		return State.ON;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return digiToRs(rsToDigi(getValueInside(EnumFacing.WEST)) ^ rsToDigi(getValueInside(EnumFacing.EAST)));
		} else {
			return 0;
		}
	}
}
