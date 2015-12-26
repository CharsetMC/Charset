package pl.asie.charset.wires.logic;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory;
import pl.asie.charset.wires.WireKind;

public class PartWireProvider implements IPartFactory {
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
    public IMultipart createPart(String id, ByteBuf buf) {
        int type = buf.readByte();
        buf.readerIndex(buf.readerIndex() - 1);
        return createPart(type);
    }

    @Override
    public IMultipart createPart(String id, NBTTagCompound nbt) {
        return createPart(nbt.getByte("t"));
    }
}
