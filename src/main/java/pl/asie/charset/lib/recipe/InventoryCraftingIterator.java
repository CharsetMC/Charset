package pl.asie.charset.lib.recipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.oredict.OreIngredient;

import java.util.*;

public class InventoryCraftingIterator extends InventoryCrafting implements Iterator<InventoryCrafting> {
    public static class Container {
        private final List stacks, output;
        private final RecipeCharset base;

        private Container(InventoryCraftingIterator iterator) {
            base = iterator.recipe;
            stacks = new ArrayList<>();

            for (int i = 0; i < iterator.recipe.input.size(); i++) {
                stacks.add(iterator.inputReal[i]);
            }

            if (iterator.permutations == 1) {
                output = iterator.recipe.getExampleOutputs();
            } else {
                output = Collections.singletonList(iterator.recipe.getCraftingResult(iterator));
            }
        }

        public List getInputs() {
            return stacks;
        }

        public List<ItemStack> getOutput() {
            return output;
        }

        public boolean isShapeless() {
            return base.getType() == RecipeCharset.Type.SHAPELESS;
        }

        public int getWidth() {
            return base.getWidth();
        }

        public int getHeight() {
            return base.getHeight();
        }
    }

    protected final RecipeCharset recipe;
    protected final Map<Ingredient, ItemStack[]> permutatingIngredients = new LinkedHashMap<>();
    protected final Object[] inputReal;
    protected int i;
    protected int permutations;

    public InventoryCraftingIterator(RecipeCharset recipe, boolean permutateAll) {
        super(new net.minecraft.inventory.Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, recipe.width, recipe.height);
        this.recipe = recipe;

        i = 0;
        permutations = 1;
        inputReal = new Object[recipe.input.size()];

        for (int i = 0; i < recipe.input.size(); i++) {
            Ingredient ing = recipe.input.get(i);
            if (!permutatingIngredients.containsKey(ing)) {
                ItemStack[] stacks = ing.getMatchingStacks();
                if (stacks.length > 1) {
                    if (!permutateAll) {
                        if (ing instanceof IngredientCharset && !((IngredientCharset) ing).mustIteratePermutations()) {
                            continue;
                        }

                        Class c = ing.getClass();
                        if (c == Ingredient.class || c == IngredientNBT.class || c == OreIngredient.class) {
                            continue;
                        }
                    }
                    permutatingIngredients.put(ing, stacks);
                    permutations *= stacks.length;
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return i < permutations;
    }

    @Override
    public InventoryCrafting next() {
        int permPos = i;
        Map<Ingredient, ItemStack> stackMap = new HashMap<>();

        for (Map.Entry<Ingredient, ItemStack[]> entry : permutatingIngredients.entrySet()) {
            ItemStack[] stacks = entry.getValue();
            stackMap.put(entry.getKey(), stacks[permPos % stacks.length]);
            permPos /= stacks.length;
        }

        for (int i = 0; i < recipe.input.size(); i++) {
            Ingredient ing = recipe.input.get(i);
            if (permutatingIngredients.containsKey(ing)) {
                setInventorySlotContents(i, stackMap.get(ing));
                inputReal[i] = stackMap.get(ing);
            } else {
                ItemStack[] stacks = ing.getMatchingStacks();
                setInventorySlotContents(i, stacks.length > 0 ? stacks[0] : ItemStack.EMPTY);
                inputReal[i] = ing;
            }
        }

        i++;
        return this;
    }

    public Container contain() {
        return new Container(this);
    }
}
