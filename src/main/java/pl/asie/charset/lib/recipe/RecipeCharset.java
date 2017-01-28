package pl.asie.charset.lib.recipe;

import com.google.common.base.Function;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeCharset extends RecipeBase {
    public enum Type {
        SHAPED,
        SHAPELESS
    }

    protected IRecipeObject[] input = null;
    protected IRecipeResult output;
    protected int width = 0;
    protected int height = 0;
    protected boolean mirrored = false;
    protected boolean shapeless = false;

    public IRecipeObject[] getInput() {
        return input;
    }

    public IRecipeResult getOutput() {
        return output;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Type getType() {
        return shapeless ? Type.SHAPELESS : Type.SHAPED;
    }

    @Override
    public boolean matches(InventoryCrafting inv, @Nullable World worldIn) {
        if (shapeless) {
            Set<IRecipeObject> objectSet = new HashSet<>();
            Collections.addAll(objectSet, input);
            for (int y = 0; y < inv.getHeight(); y++) {
                for (int x = 0; x < inv.getWidth(); x++) {
                    ItemStack stack = inv.getStackInRowAndColumn(x, y);
                    if (!stack.isEmpty()) {
                        boolean matches = false;

                        for (IRecipeObject o : objectSet) {
                            if (o.matches(stack)) {
                                matches = true;
                                objectSet.remove(o);
                                break;
                            }
                        }

                        if (!matches) {
                            return false;
                        }
                    }
                }
            }

            return objectSet.size() == 0;
        } else {
            for (int yo = 0; yo <= inv.getHeight() - height; yo++) {
                for (int xo = 0; xo <= inv.getWidth() - width; xo++) {
                    boolean noMatch = false;

                    for (int i = 0; i < input.length; i++) {
                        IRecipeObject ro = input[i];
                        int x = i % width + xo;
                        int y = i / width + yo;

                        if (ro == null) {
                            if (!inv.getStackInRowAndColumn(x, y).isEmpty()) {
                                noMatch = true;
                            }
                        } else {
                            if (!ro.matches(inv.getStackInRowAndColumn(x, y))) {
                                noMatch = true;
                            }
                        }

                        if (noMatch) {
                            break;
                        }
                    }

                    if (!noMatch) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        if (!matches(inv, null)) {
            return null;
        }

        return output.apply(inv);
    }

    @Override
    public int getRecipeSize() {
        return input.length;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    public static class Builder {
        private RecipeCharset recipe;

        private static IRecipeObject toRecipeObject(Object o) {
            if (o instanceof IRecipeObject) {
                return (IRecipeObject) o;
            } else if (o instanceof Block) {
                return new RecipeObjectItemStack((Block) o);
            } else if (o instanceof Item) {
                return new RecipeObjectItemStack((Item) o);
            } else if (o instanceof ItemStack) {
                return new RecipeObjectItemStack((ItemStack) o);
            } else if (o instanceof String) {
                return new RecipeObjectOreDict((String) o);
            } else {
                throw new RuntimeException("Invalid recipe object: " + o);
            }
        }

        private static IRecipeObject[] toRecipeObject(Object[] o) {
            IRecipeObject[] objects = new IRecipeObject[o.length];
            for (int i = 0; i < o.length; i++) {
                objects[i] = toRecipeObject(o[i]);
            }
            return objects;
        }

        public static Builder create(IRecipeResult output) {
            Builder builder = new Builder();
            builder.recipe = new RecipeCharset();
            builder.recipe.output = output;
            return builder;
        }

        public static Builder create(ItemStack output) {
            return create(new IRecipeResult() {
                @Override
                public Object preview() {
                    return output;
                }

                @Nullable
                @Override
                public ItemStack apply(@Nullable InventoryCrafting input) {
                    return output.copy();
                }
            });
        }

        public Builder shapeless(Object... o) {
            recipe.shapeless = true;
            recipe.input = toRecipeObject(o);
            return this;
        }

        public Builder shaped(Object... o) {
            int idx = 0;
            recipe.shapeless = false;
            List<String> shape = new ArrayList<>();
            List<IRecipeObject> input = new ArrayList<>();
            Map<Character, IRecipeObject> map = new HashMap<>();

            while (o[idx] instanceof String) {
                String s = (String) o[idx++];
                shape.add(s);
                recipe.width = Math.max(recipe.width, s.length());
            }
            recipe.height = shape.size();

            map.put(' ', null);

            while (idx < o.length) {
                Character c = new Character((char) o[idx++]);
                IRecipeObject ro = toRecipeObject(o[idx++]);
                map.put(c, ro);
            }

            for (int y = 0; y < recipe.height; y++) {
                String s = shape.get(y);
                for (int x = 0; x < recipe.width; x++) {
                    if (x < s.length()) {
                        input.add(map.get(s.charAt(x)));
                    } else {
                        input.add(null);
                    }
                }
            }

            recipe.input = input.toArray(new IRecipeObject[input.size()]);
            return this;
        }

        public Builder mirrored(boolean mirrored) {
            recipe.mirrored = mirrored;
            return this;
        }

        public RecipeCharset build() {
            return recipe;
        }
    }
}
