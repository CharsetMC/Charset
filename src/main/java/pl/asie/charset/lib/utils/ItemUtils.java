package pl.asie.charset.lib.utils;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemUtils {
	private ItemUtils() {

	}

	public static boolean equals(ItemStack source, ItemStack target, boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
		if (source == target) {
			return true;
		} else if (source == null || target == null) {
			return false;
		} else {
			if (source.getItem() != target.getItem()) {
				return false;
			}

			if (matchStackSize && source.stackSize != target.stackSize) {
				return false;
			}

			if (matchDamage && source.getItemDamage() != target.getItemDamage()) {
				return false;
			}

			if (matchNBT) {
				if (source.getTagCompound() == null) {
					return target.getTagCompound() == null || target.getTagCompound().hasNoTags();
				} else if (target.getTagCompound() == null) {
					return source.getTagCompound() == null || source.getTagCompound().hasNoTags();
				} else if (!source.getTagCompound().equals(target.getTagCompound())) {
					return false;
				}
			}

			return true;
		}
	}

	public static void spawnItemEntity(World world, double x, double y, double z, ItemStack stack, float mXm, float mYm, float mZm, float randomness) {
		EntityItem entityItem = new EntityItem(world, x, y, z, stack);
		entityItem.setDefaultPickupDelay();
		if (randomness <= 0.0f) {
			entityItem.motionX = mXm;
			entityItem.motionY = mYm;
			entityItem.motionZ = mZm;
		} else {
			entityItem.motionX = ((1.0f - randomness) + (((world.rand.nextDouble() - 0.5) * 2.0f) * randomness)) * mXm;
			entityItem.motionY = ((1.0f - randomness) + (((world.rand.nextDouble() - 0.5) * 2.0f) * randomness)) * mYm;
			entityItem.motionZ = ((1.0f - randomness) + (((world.rand.nextDouble() - 0.5) * 2.0f) * randomness)) * mZm;
		}
		world.spawnEntityInWorld(entityItem);
	}
}
