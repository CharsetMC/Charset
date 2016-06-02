/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.pipes;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.IMultipart;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.network.PacketPart;

public class PacketFluidUpdate extends PacketPart {
	protected PipeFluidContainer container;
	private boolean ignoreDirty;

	public PacketFluidUpdate() {
		super();
	}

	public PacketFluidUpdate(IMultipart part, PipeFluidContainer container, boolean ignoreDirty) {
		super(part);
		this.container = container;
		this.ignoreDirty = ignoreDirty;
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

		if (ignoreDirty) {
			sides = 255;
		} else {
			for (int i = 0; i <= 6; i++) {
				if (container.tanks[i].removeDirty()) {
					sides |= (1 << i);
				}
			}

			if (container.fluidDirty) {
				sides |= 128;
				container.fluidDirty = false;
			}
		}

		buf.writeByte(sides);

		if ((sides & 128) != 0) {
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
