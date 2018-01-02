package pl.asie.charset.lib.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.Comparator;
import java.util.List;

public final class SubItemSetHelper {
	private SubItemSetHelper() {

	}

	public static Comparator<ItemStack> extractMaterial(String mName, Comparator<ItemStack> next) {
		return (f, s) -> {
			ItemMaterial mf = ItemMaterialRegistry.INSTANCE.getMaterial(f.getTagCompound(), mName);
			ItemMaterial ms = ItemMaterialRegistry.INSTANCE.getMaterial(s.getTagCompound(), mName);

			return next.compare(
					mf != null ? mf.getStack() : ItemStack.EMPTY,
					ms != null ? ms.getStack() : ItemStack.EMPTY
			);
		};
	}

	public static int sortByItem(ItemStack first, ItemStack second) {
		if (first.isEmpty()) {
			return second.isEmpty() ? 0 : Integer.MIN_VALUE;
		} else if (second.isEmpty()) {
			return Integer.MAX_VALUE;
		} else if (first.getItem() == second.getItem()) {
			return first.getItemDamage() - second.getItemDamage();
		} else {
			return 65536 * (Item.getIdFromItem(first.getItem()) - Item.getIdFromItem(second.getItem()));
		}
	}

	public static int wrapLists(List<ItemStack> first, List<ItemStack> second, Comparator<ItemStack> comparator) {
		if (first.isEmpty()) {
			return second.isEmpty() ? 0 : Integer.MIN_VALUE;
		} else if (second.isEmpty()) {
			return Integer.MAX_VALUE;
		} else {
			return comparator.compare(first.get(0), second.get(0));
		}
	}
}
