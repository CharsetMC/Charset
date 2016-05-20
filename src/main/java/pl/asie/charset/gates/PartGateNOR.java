package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateNOR extends PartGate {
	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueOutside(EnumFacing.NORTH));
			case 1:
				if (!isSideOpen(EnumFacing.WEST)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.WEST));
			case 2:
				if (!isSideOpen(EnumFacing.EAST)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.EAST));
			case 3:
				if (!isSideOpen(EnumFacing.SOUTH)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.SOUTH));
			case 4:
				return State.input(getValueInside(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.NORTH)).invert();
			case 1:
				return State.input(getValueInside(EnumFacing.NORTH));
		}
		return State.ON;
	}

	@Override
	public byte calculateOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (isSideOpen(facing) && facing != EnumFacing.NORTH) {
					if (getValueInside(facing) != 0) {
						return 0;
					}
				}
			}
		}
		return 15;
	}
}
