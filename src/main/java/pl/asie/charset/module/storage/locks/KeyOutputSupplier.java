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

public class KeyOutputSupplier implements IOutputSupplier {
    private static final Random rand = new Random();
    private final ItemStack output;
    private final boolean duplicate;

    private KeyOutputSupplier(ItemStack output, boolean duplicate) {
        this.output = output;
        this.duplicate = duplicate;
    }

    @Override
    public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
        if (duplicate) {
            for (Ingredient i : matcher.getMatchedIngredients()) {
                if (i.apply(output)) {
                    ItemStack key = matcher.getStack(i);
                    ItemStack result = output.copy();
                    NBTTagCompound cpd = new NBTTagCompound();
                    cpd.setString("key", ((ItemKey) key.getItem()).getKey(key));
                    result.setTagCompound(cpd);
                    return result;
                }
            }

            return null;
        } else {
            ItemStack result = output.copy();
            NBTTagCompound cpd = new NBTTagCompound();
            cpd.setString("key", new UUID(rand.nextLong(), rand.nextLong()).toString());
            result.setTagCompound(cpd);
            return result;
        }
    }

    @Override
    public ItemStack getDefaultOutput() {
        return output;
    }

    public static class Factory implements IOutputSupplierFactory {
        @Override
        public IOutputSupplier parse(JsonContext context, JsonObject json) {
            return new KeyOutputSupplier(CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "item"), context), JsonUtils.getBoolean(json, "duplicate", false));
        }
    }
}
