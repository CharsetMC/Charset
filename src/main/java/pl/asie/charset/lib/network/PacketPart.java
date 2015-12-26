package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.UUIDUtils;

public abstract class PacketPart extends Packet {
	protected IMultipart part;

	public PacketPart() {

	}

	public PacketPart(IMultipart part) {
		this.part = part;
	}

	@Override
	public void readData(ByteBuf buf) {
		int dim = buf.readInt();
		int x = buf.readInt();
		int y = buf.readUnsignedShort();
		int z = buf.readInt();
        byte[] id = new byte[16];
        buf.readBytes(id);

		World w = ModCharsetLib.proxy.getLocalWorld(dim);

		if (w != null) {
            IMultipartContainer container = MultipartHelper.getPartContainer(w, new BlockPos(x, y, z));
            if (container != null) {
                part = container.getPartFromID(UUIDUtils.toID(id));
            }
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(part.getWorld().provider.getDimensionId());
		buf.writeInt(part.getPos().getX());
		buf.writeShort(part.getPos().getY());
		buf.writeInt(part.getPos().getZ());
        buf.writeBytes(UUIDUtils.toBytes(part.getContainer().getPartID(part)));
	}
}
