package pl.asie.charset.lib.item;

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface ISubItemProvider {
     List<ItemStack> getItems();

     default List<ItemStack> getAllItems() {
          return getItems();
     }
}
