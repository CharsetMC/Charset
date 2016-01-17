package pl.asie.charset.wires.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory;
import pl.asie.charset.wires.WireKind;

public class PartWireProvider implements IPartFactory.IAdvancedPartFactory {
	public static PartWireBase createPart(int type) {
		PartWireBase part = null;
		WireKind kind = WireKind.VALUES[type];

		switch (kind.type()) {
			case NORMAL:
				part = new PartWireNormal();
				break;
			case INSULATED:
				part = new PartWireInsulated();
				break;
			case BUNDLED:
				part = new PartWireBundled();
				break;
		}

		if (part != null) {
			part.type = kind;
		}

		return part;
	}

	@Override
	public IMultipart createPart(String id, PacketBuffer buf) {
		int type = buf.readByte();
		buf.readerIndex(buf.readerIndex() - 1);
		PartWireBase part = createPart(type);
		part.readUpdatePacket(buf);
		return part;
	}

	@Override
	public IMultipart createPart(String id, NBTTagCompound nbt) {
		PartWireBase part = createPart(nbt.getByte("t"));
		part.readFromNBT(nbt);
		return part;
	}
}
