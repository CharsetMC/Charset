package pl.asie.charset.lib.material;

import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemMaterial {
	private final String type;
	private final ItemStack stack;
	private final Map<String, ItemMaterial> typeRelations = new HashMap<>();

	public ItemMaterial(String type, ItemStack stack) {
		this.type = type;
		this.stack = stack;
	}

	public void registerRelation(ItemMaterial material, String type) {
		if (!typeRelations.containsKey(type)) {
			typeRelations.put(type, material);
		}
	}

	public ItemMaterial getRelatedByType(String type) {
		return typeRelations.get(type);
	}

	public String getType() {
		return type;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public String toString() {
		return "ItemMaterial[" + type + ":" + stack.toString() + "]";
	}
}
