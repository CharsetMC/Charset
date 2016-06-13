package pl.asie.charset.lib.wires;

import com.google.common.base.Function;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.recipe.IRecipeResult;

import javax.annotation.Nullable;

public class RecipeResultWire implements IRecipeResult {
    private final WireFactory factory;
    private final boolean freestanding;
    private final int size;

    public RecipeResultWire(WireFactory factory, boolean freestanding, int size) {
        this.factory = factory;
        this.freestanding = freestanding;
        this.size = size;
    }

    @Nullable
    @Override
    public ItemStack apply(@Nullable InventoryCrafting input) {
        return new ItemStack(
                WireManager.ITEM,
                size,
                WireManager.REGISTRY.getId(factory) << 1 | (freestanding ? 1 : 0)
        );
    }

    @Override
    public Object preview() {
        return apply(null);
    }
}
