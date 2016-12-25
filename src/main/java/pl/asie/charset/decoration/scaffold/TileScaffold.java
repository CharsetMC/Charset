package pl.asie.charset.decoration.scaffold;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pl.asie.charset.decoration.ModCharsetDecoration;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;
import pl.asie.charset.lib.utils.ItemUtils;

public class TileScaffold extends TileBase {
	public static final UnlistedPropertyGeneric<ScaffoldCacheInfo> PROPERTY = new UnlistedPropertyGeneric<>("tile", ScaffoldCacheInfo.class);
	private ItemStack plank = ItemStack.EMPTY;

	public ItemStack getPlank() {
		return plank;
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		this.plank = ItemUtils.firstNonEmpty(
				compound.hasKey("plank") ? new ItemStack(compound.getCompoundTag("plank")) : ItemStack.EMPTY,
				new ItemStack(Blocks.PLANKS));
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		ItemUtils.writeToNBT(this.plank, compound, "plank");
		return compound;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
		loadFromStack(stack);
	}

	public void loadFromStack(ItemStack stack) {
		ItemStack plank = ItemStack.EMPTY;

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("plank")) {
			plank = new ItemStack(stack.getSubCompound("plank"));
		}

		this.plank = ItemUtils.firstNonEmpty(plank, new ItemStack(Blocks.PLANKS));
	}

	@Override
	public ItemStack getDroppedBlock() {
		ItemStack stack = new ItemStack(ModCharsetDecoration.scaffoldBlock);
		stack.setTagCompound(writeNBTData(new NBTTagCompound(), false));
		return stack;
	}
}
