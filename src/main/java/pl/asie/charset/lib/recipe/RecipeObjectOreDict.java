package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

public class RecipeObjectOreDict implements IRecipeObject {
    private final int id;

    public RecipeObjectOreDict(String ore) {
        this.id = OreDictionary.getOreID(ore);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack != null && ArrayUtils.contains(OreDictionary.getOreIDs(stack), id);
    }

    @Override
    public Object preview() {
        return OreDictionary.getOres(OreDictionary.getOreName(id));
    }
}
