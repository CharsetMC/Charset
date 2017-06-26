package pl.asie.charset.lib.notify;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;

public class PacketNotification extends Packet {
	enum Type {
		COORD,
		VEC3,
		ENTITY,
		TILEENTITY,
		ONSCREEN,
		REPLACEABLE;

		static Type[] VALUES = values();
	}

	private Type type;
	private BlockPos pos;
	private Object target;
	private ItemStack item;
	private String msg;
	private String[] args;
	private ITextComponent msgComponent;
	private int msgKey;

	private EntityPlayer me;

	public PacketNotification() {

	}

	public static PacketNotification createOnscreen(String message, String[] args) {
		PacketNotification n = new PacketNotification();
		n.type = Type.ONSCREEN;
		n.msg = message;
		n.args = args;
		return n;
	}

	public static PacketNotification createReplaceable(ITextComponent msg, int msgKey) {
		PacketNotification n = new PacketNotification();
		n.type = Type.REPLACEABLE;
		n.msgComponent = msg;
		n.msgKey = msgKey;
		return n;
	}

	public static PacketNotification createNotify(Object where, ItemStack item, String format, String ...args) {
		PacketNotification n = new PacketNotification();

		if (where instanceof NotificationCoord) {
			where = ((NotificationCoord) where).getPos();
		}

		n.target = where;

		if (where instanceof Vec3d) {
			n.type = Type.VEC3;
		} else if (where instanceof BlockPos) {
			n.type = Type.COORD;
		} else if (where instanceof Entity) {
			n.type = Type.ENTITY;
		} else if (where instanceof TileEntity) {
			n.type = Type.TILEENTITY;
		} else {
			return null;
		}

		n.item = item;
		n.msg = format;
		n.args = args;
		return n;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf input) {
		me = getPlayer(handler);
		type = Type.VALUES[input.readByte()];

		switch (type) {
			case COORD:
				pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
				target = new NotificationCoord(me.world, pos);
				break;
			case ENTITY:
				int id = input.readInt();
				if (id == me.getEntityId()) {
					target = me; //bebna
				} else {
					target = me.world.getEntityByID(id);
				}
				break;
			case TILEENTITY:
				pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
				target = me.world.getTileEntity(pos);
				if (target == null) {
					target = new NotificationCoord(me.world, pos);
				}
				break;
			case VEC3:
				target = new Vec3d(input.readDouble(), input.readDouble(), input.readDouble());
				break;
			case ONSCREEN:
				msg = ByteBufUtils.readUTF8String(input);
				args = readStrings(input);
				return;
			case REPLACEABLE:
				String str = ByteBufUtils.readUTF8String(input);
				msgKey = input.readInt();
				msgComponent = ITextComponent.Serializer.jsonToComponent(str);
				return;
			default: return;
		}

		if (target == null) {
			return;
		}

		item = ByteBufUtils.readItemStack(input);
		msg = ByteBufUtils.readUTF8String(input);
		args = readStrings(input);
	}

	@Override
	public void apply(INetHandler handler) {
		switch (type) {
			case ONSCREEN:
				NotifyImplementation.proxy.onscreen(msg, args);
				break;
			case REPLACEABLE:
				NotifyImplementation.proxy.replaceable(msgComponent, msgKey);
				break;
			default:
				NotifyImplementation.recieve(me, target, item, msg, args);
				break;
		}
	}

	@Override
	public void writeData(ByteBuf output) {
		output.writeByte(type.ordinal());

		switch (type) {
			case COORD:
				BlockPos pos = (BlockPos) target;
				output.writeInt(pos.getX());
				output.writeInt(pos.getY());
				output.writeInt(pos.getZ());
				break;
			case VEC3:
				Vec3d v = (Vec3d) target;
				output.writeDouble(v.x);
				output.writeDouble(v.y);
				output.writeDouble(v.z);
				break;
			case ENTITY:
				Entity ent = (Entity) target;
				output.writeInt(ent.getEntityId());
				break;
			case TILEENTITY:
				TileEntity te = (TileEntity) target;
				output.writeInt(te.getPos().getX());
				output.writeInt(te.getPos().getY());
				output.writeInt(te.getPos().getZ());
				break;
			case ONSCREEN:
				ByteBufUtils.writeUTF8String(output, msg);
				writeStrings(output, args);
				return;
			case REPLACEABLE:
				String str = ITextComponent.Serializer.componentToJson(msgComponent);
				ByteBufUtils.writeUTF8String(output, str);
				output.writeInt(msgKey);
				return;
		}

		ByteBufUtils.writeItemStack(output, item);
		ByteBufUtils.writeUTF8String(output, msg);
		writeStrings(output, args);
	}

	@Override
	public boolean isAsynchronous() {
		return true;
	}

	private static void writeStrings(ByteBuf output, String[] args) {
		output.writeByte((byte) args.length);
		for (String s : args) {
			if (s == null) s = "null";
			ByteBufUtils.writeUTF8String(output, s);
		}
	}

	private static String[] readStrings(ByteBuf input) {
		String[] ret = new String[input.readByte()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = ByteBufUtils.readUTF8String(input);
		}
		return ret;
	}
}
