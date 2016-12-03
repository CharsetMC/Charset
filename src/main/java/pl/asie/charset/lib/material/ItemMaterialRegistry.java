package pl.asie.charset.lib.material;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.storage.barrel.BarrelRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ItemMaterialRegistry {
	public static ItemMaterialRegistry INSTANCE = new ItemMaterialRegistry();
	private final Set<ItemMaterial> materialSet = new HashSet<>();
	private final Multimap<String, ItemMaterial> materialsByType = HashMultimap.create();
	private boolean initialized = false;

	protected ItemMaterialRegistry() {

	}

	public Collection<ItemMaterial> getMaterialsByType(String type) {
		return materialsByType.get(type);
	}

	public boolean register(ItemMaterial material) {
		if (materialSet.add(material)) {
			System.out.println("Registering " + material.toString());
			materialsByType.put(material.getType(), material);
			return true;
		} else {
			return false;
		}
	}

	private void initLogMaterial(ItemStack log) {
		ItemMaterial logMaterial = new ItemMaterial("log", log);

		// We look for the plank first to ensure only valid logs
		// get registered.
		ItemStack plank = RecipeUtils.getCraftingResult(null, 3, 3, log);
		if (!plank.isEmpty() && ItemUtils.isOreType(plank, "plankWood")) {
			if (register(logMaterial)) {
				plank.setCount(1);
				ItemMaterial plankMaterial = new ItemMaterial("plank", plank);
				if (register(plankMaterial)) {
					logMaterial.registerRelation(plankMaterial, "plank");
					plankMaterial.registerRelation(logMaterial, "log");

					ItemStack slab = RecipeUtils.getCraftingResult(null, 3, 3,
							null, null, null,
							null, null, null,
							plank, plank, plank);

					if (!slab.isEmpty()) {
						slab.setCount(1);
						ItemMaterial slabMaterial = new ItemMaterial("slab", slab);
						if (register(slabMaterial)) {
							plankMaterial.registerRelation(slabMaterial, "slab");
							slabMaterial.registerRelation(plankMaterial, "block");
						}
					}

					ItemStack stick = RecipeUtils.getCraftingResult(null, 3, 3,
							plank, null, null,
							plank, null, null,
							null, null, null);
					if (stick.isEmpty()) {
						stick = new ItemStack(Items.STICK);
					} else {
						stick.setCount(1);
					}

					ItemMaterial stickMaterial = new ItemMaterial("stick", stick);
					if (register(stickMaterial)) {
						plankMaterial.registerRelation(stickMaterial, "stick");
						stickMaterial.registerRelation(plankMaterial, "plank");
						stickMaterial.registerRelation(logMaterial, "log");
					}
				}
			}
		}
	}

	private void supplyExpandedStacks(Collection<ItemStack> stacks, Consumer<ItemStack> stackConsumer) {
		for (ItemStack log : stacks) {
			try {
				if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
					for (int i = 0; i < (log.getItem() instanceof ItemBlock ? 16 : 128); i++) {
						ItemStack stack = new ItemStack(log.getItem(), 1, i);
						stackConsumer.accept(stack);
					}
				} else {
					stackConsumer.accept(log.copy());
				}
			} catch (Exception e) {

			}
		}
	}

	public void init() {
		if (initialized)
			return;

		initialized = true;
		supplyExpandedStacks(OreDictionary.getOres("logWood", false), this::initLogMaterial);
	}
}
