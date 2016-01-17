package pl.asie.charset.gates;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class PartGatePulseFormer extends PartGate {
	private byte pulse;

	public PartGatePulseFormer() {
		super();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return side == EnumFacing.SOUTH;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		tag.setByte("pl", pulse);
		super.writeToNBT(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		pulse = tag.getByte("pl");
		super.readFromNBT(tag);
	}

	@Override
	protected void onChanged() {
		if (pulse == 0) {
			boolean changed = super.tick();
			if (changed) {
				pulse = getInputInside(EnumFacing.SOUTH);
				if (pulse != 0) {
					scheduleTick();
				}
				notifyBlockUpdate();
				sendUpdatePacket();
			}
		}
	}

	@Override
	protected boolean tick() {
		boolean changed = pulse != 0;
		pulse = 0;
		changed |= super.tick();
		return changed;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.NORTH) {
			return Connection.OUTPUT;
		} else if (dir == EnumFacing.SOUTH) {
			return Connection.INPUT;
		} else {
			return Connection.NONE;
		}
	}

	@Override
	public State getLayerState(int id) {
		boolean hasSignal = getInputInside(EnumFacing.SOUTH) != 0;
		switch (id) {
			case 0:
				return State.input(getInputInside(EnumFacing.SOUTH));
			case 1:
			case 2:
				return State.bool(!hasSignal);
			case 3:
				return State.bool(hasSignal);
			case 4:
				return State.input(getOutputOutsideClient(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputInside(EnumFacing.SOUTH)).invert();
			case 1:
				return State.input(getInputInside(EnumFacing.SOUTH));
			case 2:
				return State.input(getOutputInsideClient(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte getOutputInside(EnumFacing side) {
		return pulse;
	}
}
