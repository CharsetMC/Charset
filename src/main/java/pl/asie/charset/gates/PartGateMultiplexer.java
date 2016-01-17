package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateMultiplexer extends PartGate {
	public PartGateMultiplexer() {
		super();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public State getLayerState(int id) {
		boolean isWest = getInputInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getInputInside(EnumFacing.WEST) != 0;
		boolean eastOn = getInputInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return State.input(getInputInside(EnumFacing.SOUTH));
			case 1:
				return State.input(getInputInside(EnumFacing.WEST));
			case 2:
				return State.input(getInputInside(EnumFacing.EAST));
			case 3:
				return State.bool(isWest && !westOn);
			case 4:
				return State.bool(!isWest && !eastOn);
			case 5:
				return State.input(getInputInside(EnumFacing.SOUTH)).invert();
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		boolean isWest = getInputInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getInputInside(EnumFacing.WEST) != 0;
		boolean eastOn = getInputInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return State.input(getInputInside(EnumFacing.SOUTH)).invert();
			case 1:
				return (!isWest || westOn) ? State.OFF : State.ON;
			case 2:
				return (isWest || eastOn) ? State.OFF : State.ON;
			case 3:
				return State.input(getOutputOutsideClient(EnumFacing.SOUTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte getOutputInside(EnumFacing side) {
		boolean isWest = getInputInside(EnumFacing.SOUTH) != 0;
		return isWest ? getInputInside(EnumFacing.WEST) : getInputInside(EnumFacing.EAST);
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? Connection.OUTPUT_ANALOG :
				(dir == EnumFacing.SOUTH ? Connection.INPUT : Connection.INPUT_ANALOG);
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}
}
