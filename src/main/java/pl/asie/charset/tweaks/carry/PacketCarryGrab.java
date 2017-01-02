package pl.asie.charset.tweaks.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.tweaks.ModCharsetTweaks;

/**
 * Created by asie on 1/2/17.
 */
public class PacketCarryGrab extends Packet {
	enum Type {
		BLOCK,
		ENTITY
	}

	private EntityPlayer player;
	private Type type;
	private World world;
	private BlockPos pos;
	private Entity entity;

	public PacketCarryGrab() {

	}

	public PacketCarryGrab(World world, BlockPos pos) {
		this.world = world;
		this.type = Type.BLOCK;
		this.pos = pos;
	}

	public PacketCarryGrab(World world, Entity entity) {
		this.world = world;
		this.type = Type.ENTITY;
		this.entity = entity;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dim = buf.readInt();

		player = getPlayer(handler);
		type = Type.values()[buf.readByte()];
		world = ModCharsetLib.proxy.getLocalWorld(dim);

		switch (type) {
			case BLOCK:
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				pos = new BlockPos(x, y, z);
				break;
			case ENTITY:
				int eid = buf.readInt();
				entity = world.getEntityByID(eid);
				break;
		}
	}

	@Override
	public void apply() {
		if (player != null) {
			switch (type) {
				case BLOCK:
					ModCharsetTweaks.proxy.carryGrabBlock(player, world, pos);
					break;
				case ENTITY:
					ModCharsetTweaks.proxy.carryGrabEntity(player, world, entity);
					break;
			}
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(world.provider.getDimension());
		switch (type) {
			case BLOCK:
				buf.writeByte(0);
				buf.writeInt(pos.getX());
				buf.writeInt(pos.getY());
				buf.writeInt(pos.getZ());
				break;
			case ENTITY:
				buf.writeByte(1);
				buf.writeInt(entity.getEntityId());
				break;
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
