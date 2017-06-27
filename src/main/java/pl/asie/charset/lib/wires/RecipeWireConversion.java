package pl.asie.charset.lib.wires;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import pl.asie.charset.lib.recipe.IngredientCharset;
import pl.asie.charset.lib.recipe.RecipeBase;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.module.storage.barrels.BarrelRegistry;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeWireConversion extends RecipeCharset {
    private static List<ItemStack> getMatchingStacks(int offset) {
        List<ItemStack> stacks = new ArrayList<>();
        for (WireProvider provider : WireManager.REGISTRY) {
            if (provider.hasSidedWire() && provider.hasFreestandingWire()) {
                stacks.add(new ItemStack(CharsetLibWires.itemWire, 1, (WireManager.REGISTRY.getID(provider) << 1) | offset));
            }
        }
        return stacks;
    }

    public static class IngredientWires extends IngredientCharset {
        private final int offset;

        protected IngredientWires(boolean freestanding) {
            super(0);
            offset = freestanding ? 1 : 0;
        }

        @Override
        public boolean mustIteratePermutations() {
            return true;
        }

        @Override
        public ItemStack[] getMatchingStacks() {
            Collection<ItemStack> stacks = RecipeWireConversion.getMatchingStacks(offset);
            return stacks.toArray(new ItemStack[stacks.size()]);
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

    private final int outputOffset;

    public RecipeWireConversion(boolean freestanding) {
        super("charset:wire_convert", true);
        super.input = NonNullList.create();
        super.input.add(new IngredientWires(freestanding));
        super.output = ItemStack.EMPTY;
        super.width = 1;
        super.height = 1;
        super.shapeless = true;
        this.outputOffset = freestanding ? 0 : 1;
    }

    @Override
    public List<ItemStack> getExampleOutputs() {
        return getMatchingStacks(outputOffset);
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
}
