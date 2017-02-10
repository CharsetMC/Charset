package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public interface IRecipeObject extends Predicate<ItemStack> {
    Object preview();
}
