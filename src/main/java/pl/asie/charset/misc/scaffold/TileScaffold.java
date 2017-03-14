package pl.asie.charset.misc.scaffold;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

public class TileScaffold extends TileBase {
	public static final UnlistedPropertyGeneric<ScaffoldCacheInfo> PROPERTY = new UnlistedPropertyGeneric<>("tile", ScaffoldCacheInfo.class);
	private ItemMaterial plank;

	public TileScaffold() {
		plank = getPlankFromNBT(null);
	}

	public ItemMaterial getPlank() {
		return plank;
	}

	public static ItemMaterial getPlankFromNBT(NBTTagCompound compound) {
		return ItemMaterialRegistry.INSTANCE.getMaterial(compound, "plank", "plank");
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		plank = getPlankFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		plank.writeToNBT(compound, "plank");
		return compound;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
		loadFromStack(stack);
	}

	public void loadFromStack(ItemStack stack) {
		plank = getPlankFromNBT(stack.getTagCompound());
	}

	@Override
	public ItemStack getDroppedBlock() {
		ItemStack stack = new ItemStack(CharsetMiscScaffold.scaffoldBlock);
		stack.setTagCompound(writeNBTData(new NBTTagCompound(), false));
		return stack;
	}
}
