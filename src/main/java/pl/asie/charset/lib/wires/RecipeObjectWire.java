package pl.asie.charset.lib.wires;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.recipe.IRecipeObject;
import pl.asie.charset.lib.utils.ThreeState;

import javax.annotation.Nonnull;
import java.util.Collections;

public class RecipeObjectWire implements IRecipeObject {
    private final @Nonnull WireProvider provider;
    private final @Nonnull ThreeState freestanding;

    public RecipeObjectWire(WireProvider provider, ThreeState freestanding) {
        this.provider = provider;
        this.freestanding = freestanding;
    }

    @Override
    public Object preview() {
        switch (freestanding) {
            case MAYBE:
            default:
                return Lists.newArrayList(CharsetLibWires.itemWire.toStack(provider, false, 1), CharsetLibWires.itemWire.toStack(provider, true, 1));
            case NO:
                return Collections.singletonList(CharsetLibWires.itemWire.toStack(provider, false, 1));
            case YES:
                return Collections.singletonList(CharsetLibWires.itemWire.toStack(provider, true, 1));
        }
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == CharsetLibWires.itemWire) {
            int cmpMeta = (WireManager.REGISTRY.getId(provider) << 1) | (freestanding == ThreeState.YES ? 1 : 0);
            int stackMeta = stack.getMetadata();
            if (freestanding == ThreeState.MAYBE)
                stackMeta &= (~1);
            return cmpMeta == stackMeta;
        } else {
            return false;
        }
    }
}
