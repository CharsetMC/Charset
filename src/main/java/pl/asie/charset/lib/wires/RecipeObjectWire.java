package pl.asie.charset.lib.wires;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.recipe.IRecipeObject;
import pl.asie.charset.lib.utils.MiscUtils;

public class RecipeObjectWire implements IRecipeObject {
    private final WireFactory factory;
    private final boolean freestanding;

    public RecipeObjectWire(WireFactory factory, boolean freestanding) {
        this.factory = factory;
        this.freestanding = freestanding;
    }

    @Override
    public boolean matches(ItemStack stack) {
        int id = WireManager.REGISTRY.getId(factory) << 1 | (freestanding ? 1 : 0);
        return stack != null && stack.getItem() == WireManager.ITEM && stack.getMetadata() == id;
    }
}
