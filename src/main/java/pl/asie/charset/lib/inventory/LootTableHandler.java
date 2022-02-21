/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Random;

public class LootTableHandler {
	private final IItemHandlerModifiable parent;
	private ResourceLocation lootTable;
	private long lootTableSeed;

	public LootTableHandler(IItemHandlerModifiable parent) {
		this.parent = parent;
	}

	public boolean readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("LootTable", 8)) {
			this.lootTable = new ResourceLocation(compound.getString("LootTable"));
			this.lootTableSeed = compound.getLong("LootTableSeed");
			return true;
		}
		return false;
	}

	public boolean writeToNBT(NBTTagCompound compound) {
		if (this.lootTable != null) {
			compound.setString("LootTable", this.lootTable.toString());
			if (this.lootTableSeed != 0L) {
				compound.setLong("LootTableSeed", this.lootTableSeed);
			}
			return true;
		}
		return false;
	}

	public void applyFrom(World world, @Nullable EntityPlayer player) {
		Random random;

		if (this.lootTable != null) {
			LootTable table = world.getLootTableManager().getLootTableFromLocation(this.lootTable);
			this.lootTable = null;

			if (this.lootTableSeed == 0L) {
				random = new Random();
			} else {
				random = new Random(this.lootTableSeed);
			}

			LootContext.Builder builder = new LootContext.Builder((WorldServer) world);

			if (player != null) {
				builder.withLuck(player.getLuck()).withPlayer(player); // Forge: add player to LootContext
			}

			table.fillInventory(new ReverseInvWrapper(this.parent), random, builder.build());
		}
	}

	public ResourceLocation getLootTable() {
		return this.lootTable;
	}

	public void setLootTable(ResourceLocation lootTableLocation, long lootTableSeed) {
		this.lootTable = lootTableLocation;
		this.lootTableSeed = lootTableSeed;
	}
}
