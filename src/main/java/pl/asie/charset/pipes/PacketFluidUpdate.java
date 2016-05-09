package pl.asie.charset.pipes;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.IMultipart;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.network.PacketPart;

public class PacketFluidUpdate extends PacketPart {
	protected PipeFluidContainer container;

	public PacketFluidUpdate() {
		super();
	}

	public PacketFluidUpdate(IMultipart part, PipeFluidContainer container) {
		super(part);
		this.container = container;
	}

	public void readFluidData(ByteBuf buf) {
		if (part == null || !(part instanceof PartPipe)) {
			return;
		}

		container = ((PartPipe) part).fluid;
		int sides = buf.readUnsignedByte();

		for (int i = 0; i <= 6; i++) {
			if ((sides & (1 << i)) != 0) {
				PipeFluidContainer.Tank tank = container.tanks[i];
				int amount = buf.readUnsignedShort();
				if (amount > 0) {
					int fluidID = buf.readInt();
					tank.color = buf.readInt();
					tank.stack = new FluidStack(FluidRegistry.getFluid(fluidID), amount);
				} else {
					tank.stack = null;
					tank.color = 0;
				}
			}
		}
	}

	public void writeFluidData(ByteBuf buf) {
		int sides = 0;

		for (int i = 0; i <= 6; i++) {
			if (container.tanks[i].removeDirty()) {
				sides |= (1 << i);
			}
		}

		buf.writeByte(sides);

		for (int i = 0; i <= 6; i++) {
			if ((sides & (1 << i)) != 0) {
				PipeFluidContainer.Tank tank = container.tanks[i];
				if (tank.stack == null) {
					buf.writeShort(0);
				} else {
					buf.writeShort(tank.stack.amount);
					buf.writeInt(FluidRegistry.getFluidID(tank.stack.getFluid()));
					buf.writeInt(tank.stack.getFluid().getColor(tank.stack));
				}
			}
		}
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		readFluidData(buf);
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		writeFluidData(buf);
	}
}
