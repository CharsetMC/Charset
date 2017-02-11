package pl.asie.charset.lib.recipe;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public interface IRecipeObject extends Predicate<ItemStack> {
    static IRecipeObject of(Object o) {
        if (o instanceof IRecipeObject) {
            return (IRecipeObject) o;
        } else if (o instanceof Block) {
            return new RecipeObjectItemStack((Block) o);
        } else if (o instanceof Item) {
            return new RecipeObjectItemStack((Item) o);
        } else if (o instanceof ItemStack) {
            return new RecipeObjectItemStack((ItemStack) o);
        } else if (o instanceof String) {
            return new RecipeObjectOreDict((String) o);
        } else {
            throw new RuntimeException("Invalid recipe object: " + o);
        }
    }

    Object preview();
}
