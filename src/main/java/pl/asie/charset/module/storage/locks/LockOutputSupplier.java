package pl.asie.charset.module.storage.locks;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IOutputSupplierFactory;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

import java.util.Random;
import java.util.UUID;

public class LockOutputSupplier implements IOutputSupplier {
    private final ItemStack output;

    private LockOutputSupplier(ItemStack output) {
        this.output = output;
    }

    @Override
    public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
        for (Ingredient i : matcher.getMatchedIngredients()) {
            ItemStack key = matcher.getStack(i);

            if (!key.isEmpty() && key.getItem() instanceof ItemKey) {
                ItemStack result = output.copy();
                result.setTagCompound(new NBTTagCompound());
                if (key.hasTagCompound() && key.getTagCompound().hasKey("color")) {
                    result.getTagCompound().setTag("color", key.getTagCompound().getTag("color"));
                }

                result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getKey(key));
                return result;
            }
        }

        return null;
    }

    @Override
    public ItemStack getDefaultOutput() {
        return output;
    }

    public static class Factory implements IOutputSupplierFactory {
        @Override
        public IOutputSupplier parse(JsonContext context, JsonObject json) {
            return new LockOutputSupplier(CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "item"), context));
        }
    }
}
