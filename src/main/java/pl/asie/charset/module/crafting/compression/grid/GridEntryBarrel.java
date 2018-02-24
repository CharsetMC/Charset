package pl.asie.charset.module.crafting.compression.grid;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.module.storage.barrels.BarrelUpgrade;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import java.util.Optional;

public class GridEntryBarrel extends GridEntry {
	private final TileEntityDayBarrel barrel;
	private final Orientation orientation;

	public GridEntryBarrel(TileEntityDayBarrel barrel) {
		super(barrel.getWorld(), barrel.getPos());
		this.barrel = barrel;
		this.orientation = barrel.orientation;
	}

	@Override
	public ItemStack getCraftingStack() {
		ItemStack stack = barrel.item;
		if (!stack.isEmpty()) {
			boolean copied = false;
			if (barrel.upgrades.contains(BarrelUpgrade.STICKY)) {
				stack = stack.copy();
				stack.shrink(1);
				copied = true;
			}

			if (stack.getCount() > 1) {
				if (!copied) {
					stack = stack.copy();
					copied = true;
				}
				stack.setCount(1);
			}
		}

		return stack;
	}

	@Override
	public ItemStack mergeRemainingItem(ItemStack target, boolean simulate) {
		ItemStack source = barrel.item;
		ItemStack sourceOrig = source;

		if (!source.isEmpty() && !barrel.upgrades.contains(BarrelUpgrade.INFINITE)) {
			sourceOrig = source.copy();
			if (!simulate) {
				source.shrink(1);
				barrel.setItem(source);
			}
		}

		if (target.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (ItemUtils.canMerge(sourceOrig, target)) {
			int maxGrow = Math.min(barrel.getMaxItemCount() - source.getCount(), target.getCount());
			if (maxGrow > 0) {
				if (!simulate) {
					source.grow(maxGrow);
					barrel.setItem(source);
				}

				if (maxGrow == target.getCount()) {
					return ItemStack.EMPTY;
				} else {
					ItemStack targetCopy = target.copy();
					targetCopy.shrink(maxGrow);
					return targetCopy;
				}
			} else {
				return target;
			}
		} else {
			return target;
		}
	}

	@Override
	public boolean isInvalid() {
		return barrel.isInvalid() || barrel.orientation != orientation;
	}

	@Override
	public TileEntity getTileEntity() {
		return barrel;
	}
}
