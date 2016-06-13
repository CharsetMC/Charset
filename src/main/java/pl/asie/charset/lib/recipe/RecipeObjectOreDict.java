package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.utils.MiscUtils;

public class RecipeObjectOreDict implements IRecipeObject {
    private final int id;

    public RecipeObjectOreDict(String ore) {
        this.id = OreDictionary.getOreID(ore);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack != null && MiscUtils.contains(OreDictionary.getOreIDs(stack), id);
    }
}
