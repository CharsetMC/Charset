package pl.asie.charset.lib.wires;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.recipe.IRecipeResult;

import javax.annotation.Nullable;
import java.util.Collections;

public class RecipeResultWire implements IRecipeResult {
    private final WireProvider provider;
    private final boolean freestanding;
    private final int amount;

    public RecipeResultWire(WireProvider provider, boolean freestanding, int amount) {
        this.provider = provider;
        this.freestanding = freestanding;
        this.amount = amount;
    }

    @Override
    public Object preview() {
        return apply(null);
    }

    @Nullable
    @Override
    public ItemStack apply(@Nullable InventoryCrafting input) {
        return CharsetLibWires.itemWire.toStack(provider, freestanding, amount);
    }
}
