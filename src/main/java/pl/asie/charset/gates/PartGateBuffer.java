package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateBuffer extends PartGate {
	public PartGateBuffer() {
		super();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side == EnumFacing.WEST || side == EnumFacing.EAST;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.SOUTH ? Connection.INPUT_ANALOG : Connection.OUTPUT;
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				if (!isSideOpen(EnumFacing.WEST)) {
					return State.DISABLED;
				}
				return State.input(getOutputInsideClient(EnumFacing.WEST));
			case 1:
				if (!isSideOpen(EnumFacing.EAST)) {
					return State.DISABLED;
				}
				return State.input(getOutputInsideClient(EnumFacing.EAST));
			case 2:
				return State.input(getOutputInsideClient(EnumFacing.NORTH)).invert();
			case 3:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
			case 1:
				return State.input(getOutputInsideClient(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte getOutputInside(EnumFacing side) {
		switch (side) {
			case NORTH:
				return getInputInside(EnumFacing.SOUTH) > 0 ? 0 : (byte) 15;
			case WEST:
			case EAST:
				if (isSideInverted(EnumFacing.NORTH)) {
					return getInputInside(EnumFacing.SOUTH);
				} else {
					return getInputInside(EnumFacing.SOUTH) > 0 ? 0 : (byte) 15;
				}
			default:
				return 0;
		}
	}
}
