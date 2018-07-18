/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.inventory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.function.Function;

public class GuiHandlerCharset implements IGuiHandler {
	public static final class Request {
		public final EntityPlayer player;
		public final World world;
		public final int x, y, z;
		private final int id;

		private Request(int id, EntityPlayer player, World world, int x, int y, int z) {
			this.id = id;
			this.player = player;
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public TileEntity getTileEntity() {
			return world.getTileEntity(new BlockPos(x, y, z));
		}

		public <T extends Container> T getContainer(Class<T> t) {
			Object o = GuiHandlerCharset.INSTANCE.getServerGuiElement(id, player, world, x, y, z);
			//noinspection unchecked
			return (T) o;
		}
	}

	public static final int POCKET_TABLE = 0x100;
	public static final int KEYRING = 0x101;
	public static final int CHISEL = 0x102;
	public static final int RECORD_PLAYER = 0x103;
	public static final int CHEST = 0x104;

	public static final GuiHandlerCharset INSTANCE = new GuiHandlerCharset();
	private static final TIntObjectMap<Function<Request, Object>> map = new TIntObjectHashMap<>();

	public void register(int id, Side side, Function<Request, Object> supplier) {
		int rId = id * 2 + (side == Side.CLIENT ? 1 : 0);
		if (map.containsKey(rId)) {
			throw new RuntimeException("GuiHandler ID " + id + "[" + side.name() + "] is taken by " + map.get(rId).getClass().getName() + ", tried to insert " + supplier.getClass().getName() + "!");
		}
		map.put(rId, supplier);
	}

	private GuiHandlerCharset() {

	}

	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		Function<Request, Object> supplier = map.get(id * 2);
		return supplier != null ? supplier.apply(new Request(id, player, world, x, y, z)) : null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		Function<Request, Object> supplier = map.get(id * 2 + 1);
		return supplier != null ? supplier.apply(new Request(id, player, world, x, y, z)) : null;
	}
}
