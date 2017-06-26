package pl.asie.charset.lib.wires;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import pl.asie.charset.lib.recipe.IngredientCharset;
import pl.asie.charset.lib.recipe.RecipeBase;

import javax.annotation.Nullable;

public class RecipeWireConversion extends RecipeBase {
    public static class IngredientWires extends IngredientCharset {
        private final int offset;

        protected IngredientWires(boolean freestanding) {
            super(0);
            offset = freestanding ? 1 : 0;
        }

        @Override
        public boolean apply(@Nullable ItemStack p_apply_1_) {
            if (!p_apply_1_.isEmpty() && p_apply_1_.getItem() == CharsetLibWires.itemWire && ((p_apply_1_.getMetadata() & 1) == offset)) {
                WireProvider provider = WireManager.REGISTRY.getValue(p_apply_1_.getMetadata() >> 1);
                return provider.hasFreestandingWire() && provider.hasSidedWire();
            } else {
                return false;
            }
        }
    }

    private final IngredientWires wireI;
    private final NonNullList<Ingredient> ingredients;

    public RecipeWireConversion(boolean freestanding) {
        super("charset:wire_convert", true);
        wireI = new IngredientWires(freestanding);
        ingredients = NonNullList.from(wireI);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean found = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (!found && wireI.apply(stack)) {
                    found = true;
                } else {
                    return false;
                }
            }
        }
        return found;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack newStack = stack.copy();
                newStack.setItemDamage(newStack.getItemDamage() ^ 1);
                return newStack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return (width * height) >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}
