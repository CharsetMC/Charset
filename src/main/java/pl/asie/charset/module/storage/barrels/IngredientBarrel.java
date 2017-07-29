package pl.asie.charset.module.storage.barrels;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.IngredientCharset;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class IngredientBarrel extends IngredientCharset {
    private boolean includeCarts;
    private Set<TileEntityDayBarrel.Upgrade> upgradeBlacklist;

    private static Set<TileEntityDayBarrel.Upgrade> setFromJson(JsonContext context, JsonObject jsonObject, String memberName) {
        if (jsonObject.has(memberName)) {
            ImmutableSet.Builder<TileEntityDayBarrel.Upgrade> builder = new ImmutableSet.Builder<>();
            JsonArray array = JsonUtils.getJsonArray(jsonObject, memberName);
            for (JsonElement element : array) {
                builder.add(TileEntityDayBarrel.Upgrade.valueOf(element.getAsString()));
            }
            return builder.build();
        } else {
            return Collections.emptySet();
        }
    }

    public IngredientBarrel(JsonContext context, JsonObject json) {
        super(0);
        includeCarts = JsonUtils.getBoolean(json, "carts", false);
        upgradeBlacklist = setFromJson(context, json, "upgradeBlacklist");
    }

    @Override
    public boolean mustIteratePermutations() {
        return true;
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        Collection<ItemStack> stacks = CharsetStorageBarrels.BARRELS_NORMAL;
        Collection<ItemStack> stacks2 = Lists.newArrayList();
        for (ItemStack s : stacks) {
            stacks2.add(s);
            if (includeCarts) {
                stacks2.add(CharsetStorageBarrels.barrelCartItem.makeBarrelCart(s));
            }
        }
        return stacks.toArray(new ItemStack[stacks.size()]);
    }

    @Override
    public boolean apply(ItemStack stack) {
        if (!stack.isEmpty() && (stack.getItem() == CharsetStorageBarrels.barrelItem || (includeCarts && stack.getItem() == CharsetStorageBarrels.barrelCartItem))) {
            if (!upgradeBlacklist.isEmpty()) {
                Set<TileEntityDayBarrel.Upgrade> upgrades = EnumSet.noneOf(TileEntityDayBarrel.Upgrade.class);
                TileEntityDayBarrel.populateUpgrades(upgrades, stack.getTagCompound());
                for (TileEntityDayBarrel.Upgrade upgrade : upgradeBlacklist) {
                    if (upgrades.contains(upgrade)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static class Factory implements IIngredientFactory {
        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            return new IngredientBarrel(context, json);
        }
    }
}
