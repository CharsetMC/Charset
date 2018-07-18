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

package pl.asie.charset.lib.material;

import com.google.common.collect.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.*;

public class ItemMaterialRegistry {
	public static final ItemMaterialRegistry INSTANCE = new ItemMaterialRegistry();
	private final Map<String, ItemMaterial> materialsById = new HashMap<>();
	private final ListMultimap<String, ItemMaterial> materialsByType = MultimapBuilder.hashKeys().arrayListValues().build();

	protected ItemMaterialRegistry() {

	}

	public Optional<String> getLocalizedNameFor(ItemMaterial material) {
		if (material != null) {
			String mDispName = null;
			for (String s : material.getTypes()) {
				if (I18n.canTranslate("charset.material." + s)) {
					mDispName = I18n.translateToLocal("charset.material." + s);
					break;
				}
			}

			if (mDispName == null) {
				if (material.getTypes().contains("plank")) {
					ItemMaterial alt = material.getRelated("log");
					if (alt != null) {
						material = alt;
					}
				}

				mDispName = material.getStack().getDisplayName();
			}

			return Optional.of(mDispName);
		} else {
			return Optional.empty();
		}
	}

	public String getLocalizedNameFor(String prefix, ItemMaterial material) {
		if (material != null) {
			String mDispName = null;
			for (String s : material.getTypes()) {
				if (I18n.canTranslate("charset.material." + s)) {
					mDispName = I18n.translateToLocal("charset.material." + s);
					break;
				}
			}

			if (mDispName == null) {
				if (material.getTypes().contains("plank")) {
					ItemMaterial alt = material.getRelated("log");
					if (alt != null) {
						material = alt;
					}
				}

				mDispName = material.getStack().getDisplayName();
			}

			return I18n.translateToLocalFormatted(prefix + ".format", mDispName);
		} else {
			return I18n.translateToLocal(prefix + ".name");
		}
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
		return materials != null && !materials.isEmpty() ? materials.get(0) : null;
	}

	public Collection<ItemMaterial> getMaterialsByType(String type) {
		return materialsByType.get(type);
	}

	// TODO: slow - optimize
	public Collection<ItemMaterial> getMaterialsByTypes(String... types) {
		Collection<ItemMaterial> startingPoint = materialsById.values();

		for (String type : types) {
			if (type.charAt(0) != '!') {
				Collection<ItemMaterial> collection = materialsByType.get(type);
				if (collection.size() < startingPoint.size()) {
					startingPoint = collection;
				}
			}
		}

		ImmutableSet.Builder<ItemMaterial> set = new ImmutableSet.Builder<>();
		for (ItemMaterial material : startingPoint) {
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
		if (type.length() > 0 && !material.getTypes().contains(type)) {
			material.getTypes().add(type);
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
		source.getRelations().put(relation, target);
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

	public boolean matches(ItemMaterial mat, String... mats) {
		for (String matStr : mats) {
			if (matStr.length() > 0 && matStr.charAt(0) == '!') {
				matStr = matStr.substring(1);
				if (mat.getTypes().contains(matStr)) {
					return false;
				}
			} else if (!mat.getTypes().contains(matStr)) {
				return false;
			}
		}

		return true;
	}

    public boolean matches(ItemStack stack, String... mats) {
		ItemMaterial material = getMaterialIfPresent(stack);
		if (material != null) {
			return matches(material, mats);
		}

		return false;
    }
}
