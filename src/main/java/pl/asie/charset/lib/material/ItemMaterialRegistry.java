package pl.asie.charset.lib.material;

import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;

import java.util.*;
import java.util.function.Consumer;

public class ItemMaterialRegistry {
	public static final ItemMaterialRegistry INSTANCE = new ItemMaterialRegistry();
	private final Map<String, ItemMaterial> materialsById = new HashMap<>();
	private final LinkedListMultimap<String, ItemMaterial> materialsByType = LinkedListMultimap.create();
	private final Multimap<ItemMaterial, String> materialTypes = HashMultimap.create();
	protected final Table<ItemMaterial, String, ItemMaterial> materialRelations = HashBasedTable.create();

	protected ItemMaterialRegistry() {

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
					if (srcTypes.contains(type)) {
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

	protected ItemMaterial getMaterialIfPresent(ItemStack stack) {
		return materialsById.get(createId(stack));
	}

	public ItemMaterial getOrCreateMaterial(ItemStack stack) {
		ItemMaterial material = getMaterialIfPresent(stack);
		if (material == null) {
			material = new ItemMaterial(stack);
			materialsById.put(material.getId(), material);
			ModCharsetLib.logger.info("Registered new " + material.toString());
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
}
