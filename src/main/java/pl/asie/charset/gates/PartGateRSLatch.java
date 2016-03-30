package pl.asie.charset.gates;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

public class PartGateRSLatch extends PartGate {
	private boolean toggled;
	private boolean burnt;

	@Override
	protected boolean tick() {
		boolean oldIS = getInputInside(EnumFacing.WEST) != 0;
		boolean oldIR = getInputInside(EnumFacing.EAST) != 0;

		super.tick();

		boolean newIS = getInputInside(EnumFacing.WEST) != 0;
		boolean newIR = getInputInside(EnumFacing.EAST) != 0;

		int state = ((oldIR != newIR && newIR) ? 1 : 0) | ((oldIS != newIS && newIS) ? 2 : 0);

		switch (state) {
			case 0:
			default:
				return false;
			case 1:
				if (toggled) {
					toggled = false;
					return true;
				}
				return false;
			case 2:
				if (!toggled) {
					toggled = true;
					return true;
				}
				return false;
			case 3:
				burnt = true;
				BlockPos pos = getPos();
				getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.1F, pos.getZ() + 0.5F,
						new SoundEvent(new ResourceLocation("random.fizz")), SoundCategory.BLOCKS, 0.5F, 2.6F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.8F, true);
				return true;
		}
	}

	@Override
	public void handlePacket(ByteBuf buf) {
		super.handlePacket(buf);
		burnt = buf.readBoolean();
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeBoolean(burnt);
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
	public void writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("tg", toggled);
		tag.setBoolean("br", burnt);
		super.writeToNBT(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		toggled = tag.getBoolean("tg");
		burnt = tag.getBoolean("br");
		super.readFromNBT(tag);
	}

	@Override
	public State getLayerState(int id) {
		if (burnt) {
			return State.OFF;
		}
		switch (id) {
			case 1:
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
			case 0:
				return State.input(getOutputInsideClient(EnumFacing.SOUTH));
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
				return State.input(getOutputInsideClient(EnumFacing.NORTH));
			case 1:
				return State.input(getOutputInsideClient(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public byte getOutputInside(EnumFacing facing) {
		if (burnt) {
			return 0;
		}
		return (toggled ^ (facing == EnumFacing.NORTH || facing == EnumFacing.EAST)) ? (byte) 15 : 0;
	}
}
