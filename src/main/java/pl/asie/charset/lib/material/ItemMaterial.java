package pl.asie.charset.lib.material;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;

public final class ItemMaterial {
	private final String id;
	private final ItemStack stack;

	protected ItemMaterial(ItemStack stack) {
		this.id = ItemMaterialRegistry.createId(stack);
		this.stack = stack;
	}

	public Collection<String> getTypes() {
		return ItemMaterialRegistry.INSTANCE.getMaterialTypes(this);
	}

	public ItemMaterial getRelated(String relation) {
		return ItemMaterialRegistry.INSTANCE.materialRelations.get(this, relation);
	}

	public String getId() {
		return id;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public String toString() {
		return "ItemMaterial[" + id + "]";
	}
}
