package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IngredientMatcher {
    public interface Container {
        Ingredient getIngredient(char c);
    }

    private final Container container;
    private final Map<Ingredient, ItemStack> matchedStacks = new HashMap<>();

    public IngredientMatcher(Container container) {
        this.container = container;
    }

    public Collection<Ingredient> getMatchedIngredients() {
        return matchedStacks.keySet();
    }

    public Ingredient getIngredient(char c) {
        return container.getIngredient(c);
    }

    public ItemStack getStack(Ingredient i) {
        return matchedStacks.getOrDefault(i, ItemStack.EMPTY);
    }

    public boolean add(ItemStack stack, Ingredient ingredient) {
        if (ingredient == Ingredient.EMPTY) {
            return stack.isEmpty();
        } else {
            boolean match = false;
            if (ingredient instanceof IngredientCharset) {
                IngredientCharset ic = (IngredientCharset) ingredient;
                if (((IngredientCharset) ingredient).apply(this, stack)) {
                    if (matchedStacks.containsKey(ingredient)) {
                        if (!ic.areItemStacksMatched(matchedStacks.get(ingredient), stack)) {
                            return false;
                        }
                    }

                    match = true;
                }
            } else {
                match = ingredient.apply(stack);
            }

            if (match) {
                matchedStacks.put(ingredient, stack);
                return true;
            } else {
                return false;
            }
        }
    }

    public ItemStack apply(ItemStack copy) {
        for (Map.Entry<Ingredient, ItemStack> entry : matchedStacks.entrySet()) {
            if (entry.getKey() instanceof IngredientCharset) {
                ((IngredientCharset) entry.getKey()).applyToStack(copy, entry.getValue());
            }
        }

        return copy;
    }
}
