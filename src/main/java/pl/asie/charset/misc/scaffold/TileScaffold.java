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

	public ItemMaterial getPlank() {
		return plank;
	}

	private void readPlankFromNBT(NBTTagCompound compound) {
		// TODO: Compatibility code - remove in 1.12
		plank = null;
		if (compound != null) {
			if (compound.hasKey("plank", Constants.NBT.TAG_STRING)) {
				plank = ItemMaterialRegistry.INSTANCE.getMaterial(compound.getString("plank"));
			} else if (compound.hasKey("plank", Constants.NBT.TAG_COMPOUND)) {
				plank = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(compound.getCompoundTag("plank")));
			}
		}

		if (plank == null) {
			plank = ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank");
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		readPlankFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound.setString("plank", plank.getId());
		return compound;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
		loadFromStack(stack);
	}

	public void loadFromStack(ItemStack stack) {
		readPlankFromNBT(stack.getTagCompound());
	}

	@Override
	public ItemStack getDroppedBlock() {
		ItemStack stack = new ItemStack(CharsetMiscScaffold.scaffoldBlock);
		stack.setTagCompound(writeNBTData(new NBTTagCompound(), false));
		return stack;
	}
}
