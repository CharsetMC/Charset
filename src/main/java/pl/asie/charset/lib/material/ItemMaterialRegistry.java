/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

import com.google.common.collect.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemMaterialRegistry {
	public static final ItemMaterialRegistry INSTANCE = new ItemMaterialRegistry();
	private final Map<String, ItemMaterial> materialsById = new HashMap<>();
	private final LinkedListMultimap<String, ItemMaterial> materialsByType = LinkedListMultimap.create();
	private final Multimap<ItemMaterial, String> materialTypes = MultimapBuilder.hashKeys().hashSetValues().build();
	protected final Table<ItemMaterial, String, ItemMaterial> materialRelations = HashBasedTable.create();

	protected ItemMaterialRegistry() {

	}

	private ItemMaterial getMaterialIfPresent(NBTTagCompound tag, String name) {
		ItemMaterial result = null;

		if (tag != null && tag.hasKey(name)) {
			NBTBase nameTag = tag.getTag(name);
			if (nameTag instanceof NBTTagString) {
				result = getMaterial(((NBTTagString) nameTag).getString());
			} else if (nameTag instanceof NBTTagCompound) {
				// TODO: Compatibility code! Remove in 1.13+
				ItemStack stack = new ItemStack((NBTTagCompound) nameTag);
				if (!stack.isEmpty()) {
					result = getOrCreateMaterial(stack);
				}
			}
		}

		return result;
	}

	public ItemMaterial getMaterial(NBTTagCompound tag, String name) {
		return getMaterial(tag, name, null);
	}

	public ItemMaterial getMaterial(NBTTagCompound tag, String name, String defaultType) {
		ItemMaterial result = getMaterialIfPresent(tag, name);

		return result == null ? (defaultType != null ? getDefaultMaterialByType(defaultType) : null) : result;
	}

	public ItemMaterial getMaterial(NBTTagCompound tag, String name, String defaultType, ItemStack defaultStack) {
		ItemMaterial result = getMaterialIfPresent(tag, name);
		if (result == null) {
			result = getMaterialIfPresent(defaultStack);
		}

		return result == null ? (defaultType != null ? getDefaultMaterialByType(defaultType) : null) : result;
	}

	public static final String createId(ItemStack stack) {
		StringBuilder idBuilder = new StringBuilder();
		idBuilder.append(stack.getItem().getRegistryName());
		idBuilder.append(';');
		idBuilder.append(stack.getMetadata());
		if (stack.hasTagCompound()) {
			idBuilder.append(';');
			idBuilder.append(stack.getTagCompound().toString());
		}
		return idBuilder.toString();
	}

	public ItemMaterial getDefaultMaterialByType(String type) {
		List<ItemMaterial> materials = materialsByType.get(type);
		return materials != null && materials.size() > 0 ? materials.get(0) : null;
	}

	public Collection<ItemMaterial> getMaterialsByType(String type) {
		return materialsByType.get(type);
	}

	// TODO: slow - optimize
	public Collection<ItemMaterial> getMaterialsByTypes(String... types) {
		ImmutableSet.Builder<ItemMaterial> set = new ImmutableSet.Builder<>();
		for (ItemMaterial material : materialsById.values()) {
			Collection<String> srcTypes = material.getTypes();
			boolean valid = true;
			for (String type : types) {
				if (type.charAt(0) == '!') {
					if (srcTypes.contains(type.substring(1))) {
						valid = false;
						break;
					}
				} else {
					if (!srcTypes.contains(type)) {
						valid = false;
						break;
					}
				}
			}
			if (valid) {
				set.add(material);
			}
		}
		return set.build();
	}

	public ItemMaterial getMaterial(String id) {
		return materialsById.get(id);
	}

	public Collection<String> getMaterialTypes(ItemMaterial material) {
		return materialTypes.get(material);
	}

	public ItemMaterial getMaterialIfPresent(ItemStack stack) {
		return materialsById.get(createId(stack));
	}

	public ItemMaterial getOrCreateMaterial(ItemStack stack) {
		ItemMaterial material = getMaterialIfPresent(stack);
		if (material == null) {
			material = new ItemMaterial(stack);
			materialsById.put(material.getId(), material);
		}
		return material;
	}

	public boolean registerTypes(ItemMaterial material, String... types) {
		boolean result = false;
		for (String type : types) {
			result |= registerType(material, type);
		}
		return result;
	}

	public boolean registerType(ItemMaterial material, String type) {
		if (type.length() > 0 && !materialTypes.containsEntry(material, type)) {
			materialTypes.put(material, type);
			materialsByType.put(type, material);
			return true;
		} else {
			return false;
		}
	}

	public boolean registerRelation(ItemMaterial source, ItemMaterial target, String relation, String invRelation) {
		return registerRelation(source, target, relation) && registerRelation(target, source, invRelation);
	}

	public boolean registerRelation(ItemMaterial source, ItemMaterial target, String relation) {
		materialRelations.put(source, relation, target);
		return true;
	}

	public Collection<String> getAllTypes() {
		return materialsByType.keySet();
	}

	public Collection<ItemMaterial> getAllMaterials() {
		return materialsById.values();
	}

	public boolean matches(ItemStack stack, ItemMaterial mat) {
		return ItemUtils.canMerge(mat.getStack(), stack);
	}

    public boolean matches(ItemStack stack, String... mats) {
		for (String mat : mats) {
			boolean valid = true;
			if (mat.startsWith("!")) {
				mat = mat.substring(1);
				valid = false;
			}

			for (ItemMaterial material : materialsByType.get(mat)) {
				if (ItemUtils.canMerge(material.getStack(), stack)) {
					return valid;
				}
			}
		}

		return false;
    }
}
