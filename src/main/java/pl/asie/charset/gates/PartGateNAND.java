package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateNAND extends PartGate {
	public PartGateNAND() {
		super();
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
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
				return State.input(getOutputOutsideClient(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputInside(EnumFacing.WEST)).invert();
			case 1:
				return State.input(getInputInside(EnumFacing.SOUTH)).invert();
			case 2:
				return State.input(getInputInside(EnumFacing.EAST)).invert();
			case 3:
				return State.input(getOutputInsideClient(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte getOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (isSideOpen(facing) && facing != EnumFacing.NORTH) {
					if (getInputInside(facing) == 0) {
						return 15;
					}
				}
			}
		}
		return 0;
	}
}
