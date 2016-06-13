package pl.asie.charset.lib.recipe;

import com.google.common.base.Function;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public interface IRecipeResult extends Function<InventoryCrafting, ItemStack> {
    Object preview();
}
