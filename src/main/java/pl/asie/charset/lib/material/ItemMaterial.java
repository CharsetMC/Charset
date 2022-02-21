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

package pl.asie.charset.lib.material;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public final class ItemMaterial {
	private final ItemStack stack;

	private final transient String id;
	private final transient String idForTexture;
	private final transient Set<String> types;
	private final transient Map<String, ItemMaterial> relations;

	protected ItemMaterial(ItemStack stack) {
		this.stack = stack;
		this.id = ItemMaterialRegistry.createId(stack);
		this.idForTexture = this.id.replaceAll("[^0-9a-zA-Z]+", "_");
		this.types = new HashSet<>();
		this.relations = new HashMap<>();
	}

	public Collection<String> getTypes() {
		return types;
	}

	public Map<String, ItemMaterial> getRelations() {
		return relations;
	}

	public ItemMaterial getRelated(String relation) {
		return relations.get(relation);
	}

	public String getId() {
		return id;
	}

	public String getIdForTexture() {
		return this.idForTexture;
	}

	public ItemStack getStack() {
		return stack;
	}

	public void writeToNBT(NBTTagCompound compound, String key) {
		compound.setString(key, id);
	}

	@Override
	public String toString() {
		return "ItemMaterial[" + id + "]";
	}
}
