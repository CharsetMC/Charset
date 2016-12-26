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

package pl.asie.charset.pipes.pipe;

import java.lang.ref.WeakReference;

import io.netty.buffer.ByteBuf;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.network.PacketTile;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.pipes.PipeUtils;

public class PacketItemUpdate extends PacketTile {
	protected PipeItem item;
	private static final TIntObjectMap<WeakReference<PipeItem>> itemIdCache = new TIntObjectHashMap<WeakReference<PipeItem>>();
	private boolean syncStack;

	private ItemStack stack;
	private short id;
	private int dirs, flags, progress;
	private boolean addWhenDone;

	public PacketItemUpdate() {
		super();
	}

	public PacketItemUpdate(TileEntity part, PipeItem item, boolean syncStack) {
		super(part);
		this.item = item;
		this.syncStack = syncStack;
	}

	public void readItemData(ByteBuf buf) {
		id = buf.readShort();
		dirs = buf.readUnsignedByte();
		flags = buf.readUnsignedByte();
		progress = buf.readUnsignedByte();
		addWhenDone = false;
		syncStack = (flags & 0x04) != 0;

		if (syncStack) {
			stack = ByteBufUtils.readItemStack(buf);
		}
	}

	public void writeItemData(ByteBuf buf) {
		buf.writeShort(item.id);
		buf.writeByte(SpaceUtils.ordinal(item.input) | (SpaceUtils.ordinal(item.output) << 3));
		buf.writeByte((item.reachedCenter ? 0x01 : 0) | (item.isStuck() ? 0x02 : 0) | (syncStack ? 0x04 : 0));
		buf.writeByte(item.progress);

		if (syncStack) {
			ByteBufUtils.writeItemStack(buf, item.getStack());
		}
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		readItemData(buf);
	}

	@Override
	public void apply() {
		WeakReference<PipeItem> ref = itemIdCache.get(id);
		item = ref != null ? ref.get() : null;

		if (item != null && (item.getOwner() != tile || !item.getOwner().getPipeItems().contains(item))) {
			item.getOwner().removeItemClientSide(item);
			itemIdCache.remove(id);
			item = null;
		}

		TilePipe pipe = PipeUtils.getPipe(tile);
		if (pipe == null) {
			return;
		}

		if (item == null) {
			item = pipe.getItemByID(id);
		}

		if (item == null) {
			if (syncStack) {
				item = new PipeItem(pipe, id);
				addWhenDone = true;
			} else {
				return;
			}
		}

		item.input = SpaceUtils.getFacing(dirs & 7);
		item.output = SpaceUtils.getFacing((dirs >> 3) & 7);
		item.reachedCenter = (flags & 0x01) != 0;
		item.blocksSinceSync = 0;
		if (addWhenDone) {
			item.progress = progress;
		}

		boolean stuck = (flags & 0x02) != 0;

		item.setStuckFlagClient(stuck);

		if (syncStack) {
			item.stack = stack;
		}

		itemIdCache.put(id, new WeakReference<PipeItem>(item));

		if (addWhenDone) {
			pipe.addItemClientSide(item);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		writeItemData(buf);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
