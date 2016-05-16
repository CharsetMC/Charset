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

		if ((sides & 128) != 0) {
			int fluidID = buf.readInt();
			if (fluidID >= 0) {
				container.fluidColor = buf.readInt();
				container.fluidStack = new FluidStack(FluidRegistry.getFluid(fluidID), 1000);
			} else {
				container.fluidColor = 0;
				container.fluidStack = null;
			}
		}

		for (int i = 0; i <= 6; i++) {
			if ((sides & (1 << i)) != 0) {
				PipeFluidContainer.Tank tank = container.tanks[i];
				tank.amount = buf.readUnsignedShort();
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

		if (container.fluidDirty) {
			sides |= 128;
		}

		buf.writeByte(sides);

		if (container.fluidDirty) {
			container.fluidDirty = false;
			if (container.fluidStack == null) {
				buf.writeInt(-1);
			} else {
				buf.writeInt(FluidRegistry.getFluidID(container.fluidStack.getFluid()));
				buf.writeInt(container.fluidStack.getFluid().getColor(container.fluidStack));
			}
		}

		for (int i = 0; i <= 6; i++) {
			if ((sides & (1 << i)) != 0) {
				buf.writeShort(container.tanks[i].amount);
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
