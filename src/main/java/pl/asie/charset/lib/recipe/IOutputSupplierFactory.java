package pl.asie.charset.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.JsonContext;

@FunctionalInterface
public interface IOutputSupplierFactory {
    IOutputSupplier parse(JsonContext context, JsonObject json);
}
