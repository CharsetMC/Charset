package pl.asie.charset.module.decoration.stacks;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.Trait;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.Map;

public class TileEntityStacks extends TileBase {
	protected final NonNullList<ItemMaterial> stacks = NonNullList.create();

	public TileEntityStacks() {

	}

	public static boolean canAcceptStackType(ItemStack stack) {
		if (stack.getCount() != 1) {
			return false;
		}

		ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
		if (material == null || !(material.getTypes().contains("ingot"))) {
			return false;
		}

		return true;
	}

	public boolean offerStack(ItemStack stack) {
		if (!canAcceptStackType(stack) || stacks.size() >= 64) {
			return false;
		}

		ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
		stacks.add(material);
		markBlockForUpdate();
		return true;
	}

	public ItemStack removeStack(boolean simulate) {
		if (simulate) {
			return stacks.get(stacks.size() - 1).getStack();
		} else {
			ItemStack stack = stacks.get(stacks.size() - 1).getStack();
			stacks.remove(stacks.size() - 1);
			markBlockForUpdate();
			return stack;
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		stacks.clear();
		NBTTagList list = compound.getTagList("stacks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound cpd = list.getCompoundTagAt(i);
			stacks.add(ItemMaterialRegistry.INSTANCE.getMaterial(cpd, "material"));
		}
		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		NBTTagList list = new NBTTagList();
		for (ItemMaterial stack : stacks) {
			NBTTagCompound cpd = new NBTTagCompound();
			stack.writeToNBT(cpd, "material");
			list.appendTag(cpd);
		}
		compound.setTag("stacks", list);
		return compound;
	}
}
