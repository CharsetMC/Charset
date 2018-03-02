package pl.asie.simplelogic.gates.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import pl.asie.simplelogic.gates.PartGate;

import java.util.Random;

public class GateLogicRandomizer extends GateLogic {
	private static final Random rand = new Random();

	@Override
	public boolean tick(PartGate parent) {
		byte oldInput = getInputValueInside(EnumFacing.SOUTH);
		parent.updateInputs(inputValues);
		byte newInput = getInputValueInside(EnumFacing.SOUTH);
		if (newInput != oldInput && newInput > 0) {
			// generate random values
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (facing != EnumFacing.SOUTH) {
					int r;
					if (newInput <= 8) {
						r = rand.nextInt(16);
					} else {
						r = rand.nextBoolean() ? 15 : 0;
					}
					outputValues[facing.ordinal() - 2] = (byte) r;
				}
			}
			return true;
		} else {
			return newInput != oldInput;
		}
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side != EnumFacing.SOUTH;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.SOUTH ? Connection.INPUT_ANALOG : Connection.OUTPUT_ANALOG;
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
				return State.input(getOutputValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 3:
				return State.input(getOutputValueInside(EnumFacing.EAST));
		}
		return null;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputValueInside(EnumFacing.WEST));
			case 1:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 2:
				return State.input(getOutputValueInside(EnumFacing.EAST));
		}
		return null;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		return 0;
	}
}
