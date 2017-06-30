package pl.asie.charset.lib.item;

import net.minecraft.item.ItemStack;
import java.util.Collection;

@FunctionalInterface
public interface ISubItemProvider {
     Collection<ItemStack> getItems();

     default Collection<ItemStack> getAllItems() {
          return getItems();
     }
}
