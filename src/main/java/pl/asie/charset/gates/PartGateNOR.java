package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateNOR extends PartGate {
	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputOutsideClient(EnumFacing.NORTH));
			case 1:
				if (!isSideOpen(EnumFacing.WEST)) {
					return State.DISABLED;
				}
				return State.input(getInputInside(EnumFacing.WEST));
			case 2:
				if (!isSideOpen(EnumFacing.EAST)) {
					return State.DISABLED;
				}
				return State.input(getInputInside(EnumFacing.EAST));
			case 3:
				if (!isSideOpen(EnumFacing.SOUTH)) {
					return State.DISABLED;
				}
				return State.input(getInputInside(EnumFacing.SOUTH));
			case 4:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputInsideClient(EnumFacing.NORTH)).invert();
			case 1:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
		}
		return State.ON;
	}

	@Override
	public byte getOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (isSideOpen(facing) && facing != EnumFacing.NORTH) {
					if (getInputInside(facing) != 0) {
						return 0;
					}
				}
			}
		}
		return 15;
	}
}
