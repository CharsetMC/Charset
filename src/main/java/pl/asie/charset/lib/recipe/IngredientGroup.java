package pl.asie.charset.lib.recipe;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;
import pl.asie.charset.lib.recipe.ingredient.IngredientWrapper;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import java.util.*;

// TODO: This is a very early version. To be improved!
public class IngredientGroup extends IngredientCharset {
    private static class Entry {
        private final TIntObjectMap<List> typeMap = new TIntObjectHashMap<>();
        private final TIntObjectMap<String> idNameMap;
        private final TObjectIntMap<String> nameIdMap;
        private int maxNameId = 0;

        public Entry(boolean stringBased) {
            if (stringBased) {
                idNameMap = new TIntObjectHashMap<>();
                nameIdMap = new TObjectIntHashMap<>();
            } else {
                idNameMap = null;
                nameIdMap = null;
            }
        }

        public boolean isStringBased() {
            return idNameMap != null;
        }
    }

    private static final Map<String, Entry> entryMap = new HashMap<>();
    private final String type, nbtTag;
    private final TIntSet blacklistedIds;
    private final boolean modifyMeta;
    private char dependencyChar;
    private Ingredient dependency;

    public IngredientGroup(String type, String nbtTag, boolean modifyMeta, TIntSet blacklistedIds, char depChar) {
        super();
        this.type = type;
        this.nbtTag = nbtTag;
        this.modifyMeta = modifyMeta;
        this.blacklistedIds = blacklistedIds;
        this.dependencyChar = depChar;
    }

    @Override
    public void onAdded(IRecipeView view) {
        if (dependencyChar != '\0') {
            dependency = view.getIngredient(dependencyChar);
        }
    }

    public static void register(String type, String id, Object... objects) {
        Entry e = entryMap.get(type);
        if (e == null) {
            e = new Entry(true);
            entryMap.put(type, e);
        } else if (!e.isStringBased()) {
            throw new RuntimeException("Trying to register string id on int-based entry!");
        }
        for (Object object : objects) {
            registerBody(e, e.maxNameId++, object, id);
        }
    }

    public static void register(String type, int id, Object... objects) {
        Entry e = entryMap.get(type);
        if (e == null) {
            e = new Entry(false);
            entryMap.put(type, e);
        } else if (e.isStringBased()) {
            throw new RuntimeException("Trying to register int id on string-based entry!");
        }
        for (Object object : objects) {
            registerBody(e, id, object, null);
        }
    }

    private static void registerBody(Entry e, int id, Object object, String name) {
        if (object instanceof String || object instanceof ItemStack) {
            if (!e.typeMap.containsKey(id)) {
                e.typeMap.put(id, new ArrayList());
            }
            e.typeMap.get(id).add(object);
            if (e.isStringBased()) {
                e.idNameMap.put(id, name);
                e.nameIdMap.put(name, id);
            }
        } else {
            throw new RuntimeException("Unknown/unaccepted type: " + object.getClass().getName() + "!");
        }
    }

    public static void registerDefaults() {
        for (int i = -1; i < 16; i++) {
            String oreSuffix = i == -1 ? "Colorless" : ColorUtils.getOreDictEntry("", EnumDyeColor.byMetadata(i));
            register("glass", i, "blockGlass" + oreSuffix);
            register("glassPane", i, "paneGlass" + oreSuffix);
            if (i > 0) {
                register("dye", i, "dye" + oreSuffix);
                register("wool", i, new ItemStack(Blocks.WOOL, 1, 15 - i));
                register("hardenedClay", i, new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i));
            } else {
                register("hardenedClay", i, new ItemStack(Blocks.HARDENED_CLAY));
            }
        }
    }

    private int getId(ItemStack stack) {
        Entry e = entryMap.get(type);
        if (stack.isEmpty() || e == null) {
            return Integer.MIN_VALUE;
        }

        int[] ids = OreDictionary.getOreIDs(stack);

        TIntObjectIterator<List> iterator = e.typeMap.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (blacklistedIds.contains(iterator.key())) {
                continue;
            }
            Collection c = iterator.value();
            for (Object o : c) {
                if (o instanceof String) {
                    int targetId = OreDictionary.getOreID((String) o);
                    for (int id : ids) {
                        if (targetId == id) {
                            return iterator.key();
                        }
                    }
                } else if (o instanceof ItemStack) {
                    if (ItemUtils.canMerge(stack, (ItemStack) o)) {
                        return iterator.key();
                    }
                }
            }
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public ItemStack transform(ItemStack stack, ItemStack source, IRecipeResultBuilder builder) {
        if (nbtTag != null) {
            int id = getId(source);
            Entry e = entryMap.get(type);
            if (e != null && e.isStringBased()) {
                ItemUtils.getTagCompound(stack, true).setString(nbtTag, e.idNameMap.get(id));
            } else {
                ItemUtils.getTagCompound(stack, true).setInteger(nbtTag, id);
            }
        } else if (modifyMeta) {
            int id = getId(source);
            Entry e = entryMap.get(type);
            if (e != null) {
                stack.setItemDamage(stack.getItemDamage() + id);
            }
        }
        return stack;
    }

    @Override
    public boolean arePermutationsDistinct() {
        return true;
    }

    @Override
    public boolean matchSameGrid(ItemStack a, ItemStack b) {
        return getId(a) == getId(b);
    }

    @Override
    public boolean matches(ItemStack stack, IRecipeResultBuilder builder) {
        int id = getId(stack);
        if (dependency != null && dependency instanceof IngredientWrapper && ((IngredientWrapper) dependency).getIngredientCharset() instanceof IngredientGroup) {
            if (id != ((IngredientGroup) ((IngredientWrapper) dependency).getIngredientCharset()).getId(builder.getStack(dependency))) {
                return false;
            }
        }

        return id != Integer.MIN_VALUE;
    }

    @Override
    public ItemStack[][] getMatchingStacks() {
        TIntList list = new TIntArrayList();

        Entry e = entryMap.get(type);
        if (e == null) {
            return new ItemStack[0][0];
        }
        TIntIterator iterator = e.typeMap.keySet().iterator();
        while (iterator.hasNext()) {
            int id = iterator.next();
            if (!blacklistedIds.contains(id)) {
                list.add(id);
            }
        }

        ItemStack[][] stacks = new ItemStack[list.size()][];
        for (int i = 0; i < stacks.length; i++) {
            Collection c = e.typeMap.get(list.get(i));
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
            String type = JsonUtils.getString(json, "group");
            String tag = json.has("nbtKey") ? JsonUtils.getString(json, "nbtKey") : null;

            TIntSet blacklistedIds = new TIntHashSet();
            if (JsonUtils.hasField(json, "blacklist")) {
                JsonArray array = JsonUtils.getJsonArray(json, "blacklist");
                for (JsonElement e : array) {
                    blacklistedIds.add(e.getAsInt());
                }
            }

            char dep = '\0';

            if (JsonUtils.hasField(json, "depends")) {
                dep = JsonUtils.getString(json, "depends").charAt(0);
            }

            return IngredientCharset.wrap(new IngredientGroup(type, tag, json.has("modifyMeta"), blacklistedIds, dep));
        }
    }
}
