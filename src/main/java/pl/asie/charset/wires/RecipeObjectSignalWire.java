package pl.asie.charset.wires;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.recipe.IRecipeObject;
import pl.asie.charset.lib.wires.PartWire;
import pl.asie.charset.lib.wires.RecipeObjectWire;
import pl.asie.charset.lib.wires.WireFactory;
import pl.asie.charset.lib.wires.WireManager;
import pl.asie.charset.wires.logic.PartWireSignalBase;
import pl.asie.charset.wires.logic.WireSignalFactory;

/**
 * Created by asie on 6/13/16.
 */
public class RecipeObjectSignalWire implements IRecipeObject {
    private final WireType type;
    private final boolean freestanding;

    public RecipeObjectSignalWire(WireType type, boolean freestanding) {
        this.type = type;
        this.freestanding = freestanding;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack != null && stack.stackSize == 1 && stack.getItem() == WireManager.ITEM) {
            WireFactory factory = WireManager.ITEM.getFactory(stack);
            if (factory instanceof WireSignalFactory) {
                WireSignalFactory signalFactory = (WireSignalFactory) factory;
                if (signalFactory.type == type) {
                    boolean targetFreestanding = WireManager.ITEM.isFreestanding(stack);
                    return freestanding ? targetFreestanding : !targetFreestanding;
                }
            }
        }

        return false;
    }
}
