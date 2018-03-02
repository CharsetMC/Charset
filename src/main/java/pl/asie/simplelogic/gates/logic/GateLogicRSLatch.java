package pl.asie.simplelogic.gates.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import pl.asie.simplelogic.gates.PartGate;

import java.util.Arrays;

public class GateLogicRSLatch extends GateLogic {
	private boolean toggled;
	private boolean burnt;

	@Override
	public boolean tick(PartGate parent) {
		boolean oldIS = getInputValueInside(EnumFacing.WEST) != 0;
		boolean oldIR = getInputValueInside(EnumFacing.EAST) != 0;

		boolean changed = false;

		if (parent.updateInputs(inputValues)) {
			changed = true;
		}

		boolean newIS = getInputValueInside(EnumFacing.WEST) != 0;
		boolean newIR = getInputValueInside(EnumFacing.EAST) != 0;

		int state = ((oldIR != newIR && newIR) ? 1 : 0) | ((oldIS != newIS && newIS) ? 2 : 0);

		switch (state) {
			case 0:
			default:
				break;
			case 1:
				toggled = false;
				break;
			case 2:
				toggled = true;
				break;
			case 3:
				//burnt = true;
				//BlockPos pos = parent.getPos();
				//parent.getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.1F, pos.getZ() + 0.5F,
				//		new SoundEvent(new ResourceLocation("random.fizz")), SoundCategory.BLOCKS, 0.5F, 2.6F + (parent.getWorld().rand.nextFloat() - parent.getWorld().rand.nextFloat()) * 0.8F, true);

				// haha, JK
				toggled = !toggled;
				break;
		}

		return updateOutputs();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return false;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir.getAxis() == EnumFacing.Axis.X ? Connection.INPUT_OUTPUT : Connection.OUTPUT;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		super.writeToNBT(tag, isClient);
		tag.setBoolean("tg", toggled);
		tag.setBoolean("br", burnt);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, boolean isClient) {
		toggled = tag.getBoolean("tg");
		burnt = tag.getBoolean("br");
		super.readFromNBT(tag, isClient);
	}

	@Override
	public State getLayerState(int id) {
		if (burnt) {
			return State.OFF;
		}
		switch (id) {
			case 1:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 0:
				return State.input(getOutputValueInside(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public State getTorchState(int id) {
		if (burnt) {
			return State.OFF;
		}
		switch (id) {
			case 0:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 1:
				return State.input(getOutputValueInside(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (burnt) {
			return 0;
		}
		return (toggled ^ (facing == EnumFacing.NORTH || facing == EnumFacing.EAST)) ? (byte) 15 : 0;
	}
}
