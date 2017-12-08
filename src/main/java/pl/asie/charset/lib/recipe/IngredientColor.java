package pl.asie.charset.lib.recipe;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import java.util.Collection;

// TODO: This is a very early version. To be improved!
public class IngredientColor extends IngredientCharset {
    // We map Strings + int<0, 16> to either a List<ItemStack> or a String (proxies to List<ItemStack>)
    private static final Multimap[] colorMaps = new Multimap[17];
    private final String type, nbtTag;
    private final boolean allowColorless;

    public IngredientColor(String type, String nbtTag, boolean allowColorless) {
        super();
        this.type = type;
        this.nbtTag = nbtTag;
        this.allowColorless = allowColorless;
    }

    static {
        for (int i = 0; i <= 16; i++) {
            colorMaps[i] = HashMultimap.create();
        }
    }

    public static void register(String type, int color, Object object) {
        if (object instanceof String || object instanceof ItemStack) {
            colorMaps[color].put(type, object);
        } else {
            throw new RuntimeException("Unknown/unaccepted type: " + object.getClass().getName() + "!");
        }
    }

    public static void registerDefaults() {
        for (int i = 0; i < 17; i++) {
            String oreSuffix = i == 0 ? "Colorless" : ColorUtils.getOreDictEntry("", EnumDyeColor.byMetadata(i - 1));
            register("glass", i, "blockGlass" + oreSuffix);
            register("glassPane", i, "paneGlass" + oreSuffix);
            if (i > 0) {
                register("dye", i, "dye" + oreSuffix);
                register("wool", i, new ItemStack(Blocks.WOOL, 1, 15 - (i - 1)));
                register("hardenedClay", i, new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i));
            } else {
                register("hardenedClay", i, new ItemStack(Blocks.HARDENED_CLAY));
            }
        }
    }

    private int getColor(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }

        int[] ids = OreDictionary.getOreIDs(stack);
        for (int i = allowColorless ? 0 : 1; i <= 16; i++) {
            Collection c = colorMaps[i].get(type);
            for (Object o : c) {
                if (o instanceof String) {
                    int targetId = OreDictionary.getOreID((String) o);
                    for (int id : ids) {
                        if (targetId == id) {
                            return i;
                        }
                    }
                } else if (o instanceof ItemStack) {
                    if (ItemUtils.canMerge(stack, (ItemStack) o)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public ItemStack transform(ItemStack stack, ItemStack source, IRecipeResultBuilder builder) {
        if (nbtTag != null) {
            ItemUtils.getTagCompound(stack, true).setInteger("color", getColor(source) - 1);
        }
        return stack;
    }

    @Override
    public boolean arePermutationsDistinct() {
        return true;
    }

    @Override
    public boolean matchSameGrid(ItemStack a, ItemStack b) {
        return getColor(a) == getColor(b);
    }

    @Override
    public boolean matches(ItemStack stack, IRecipeResultBuilder builder) {
        return getColor(stack) >= 0;
    }

    @Override
    public ItemStack[][] getMatchingStacks() {
        ItemStack[][] stacks = new ItemStack[allowColorless ? 17 : 16][];
        for (int i = 0; i < stacks.length; i++) {
            if (i == 0 && !allowColorless) {
                stacks[0] = new ItemStack[0];
                continue;
            }

            Multimap multimap = colorMaps[allowColorless ? i : i + 1];
            Collection c = multimap.get(type);
            int length = 0;

            for (Object o : c) {
                if (o instanceof String) {
                    length += OreDictionary.getOres((String) o).size();
                } else if (o instanceof ItemStack) {
                    length++;
                }
            }

            stacks[i] = new ItemStack[length];
            int j = 0;

            for (Object o : c) {
                if (o instanceof String) {
                    NonNullList<ItemStack> stackList = OreDictionary.getOres((String) o);
                    for (ItemStack stack : stackList) {
                        stacks[i][j++] = stack;
                    }
                } else if (o instanceof ItemStack) {
                    stacks[i][j++] = (ItemStack) o;
                }
            }
        }

        return stacks;
    }

    public static class Factory implements IIngredientFactory {
        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            String type = JsonUtils.getString(json, "color");
            String tag = "color";
            if (JsonUtils.hasField(json, "nbtKey")) {
                tag = JsonUtils.getString(json, "nbtKey");
            }
            boolean allowColorless = JsonUtils.getBoolean(json, "allowColorless", false);

            return IngredientCharset.wrap(new IngredientColor(type, tag, allowColorless));
        }
    }
}
