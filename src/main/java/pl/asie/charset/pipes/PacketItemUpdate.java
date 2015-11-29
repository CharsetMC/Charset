package pl.asie.charset.pipes;

import java.lang.ref.WeakReference;

import io.netty.buffer.ByteBuf;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import net.minecraftforge.common.util.ForgeDirection;

import pl.asie.charset.lib.network.PacketTile;

public class PacketItemUpdate extends PacketTile {
	protected PipeItem item;
	private static final TIntObjectMap<WeakReference<PipeItem>> itemIdCache = new TIntObjectHashMap<WeakReference<PipeItem>>();
	private boolean syncStack;

	public PacketItemUpdate() {
		super();
	}

	public PacketItemUpdate(TileEntity tile, PipeItem item, boolean syncStack) {
		super(tile);
		this.item = item;
		this.syncStack = syncStack;
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);

		if (tile == null || !(tile instanceof TilePipe)) {
			return;
		}

		short id = buf.readShort();
		int dirs = buf.readUnsignedByte();
		int flags = buf.readUnsignedByte();
		int progress = buf.readUnsignedByte();
		boolean addWhenDone = false;

		WeakReference<PipeItem> ref = itemIdCache.get(id);
		item = ref != null ? ref.get() : null;
		syncStack = (flags & 0x04) != 0;

		if (item != null && (item.getOwner() != tile || !item.getOwner().getPipeItems().contains(item))) {
			item.getOwner().removeItemClientSide(item);
			itemIdCache.remove(id);
			item = null;
		}

		if (item == null) {
			TilePipe pipe = (TilePipe) tile;
			for (PipeItem p : pipe.getPipeItems()) {
				if (p.id == id) {
					item = p;
					break;
				}
			}
		}

		if (item == null) {
			if (syncStack) {
				item = new PipeItem((TilePipe) tile, id);
				addWhenDone = true;
			} else {
				return;
			}
		}

		item.blocksSinceSync = 0;
		item.input = ForgeDirection.getOrientation(dirs & 7);
		item.output = ForgeDirection.getOrientation((dirs >> 3) & 7);
		item.reachedCenter = (flags & 0x01) != 0;
		item.progress = progress;

		boolean stuck = (flags & 0x02) != 0;

		item.setStuckFlagClient(stuck);

		if (syncStack) {
			item.stack = ByteBufUtils.readItemStack(buf);
		}

		itemIdCache.put(id, new WeakReference<PipeItem>(item));

		if (addWhenDone) {
			((TilePipe) tile).addItemClientSide(item);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);

		buf.writeShort(item.id);
		buf.writeByte(item.input.ordinal() | (item.output.ordinal() << 3));
		buf.writeByte((item.reachedCenter ? 0x01 : 0) | (item.isStuck() ? 0x02 : 0) | (syncStack ? 0x04 : 0));
		buf.writeByte(item.progress);

		if (syncStack) {
			ByteBufUtils.writeItemStack(buf, item.getStack());
		}
	}
}
