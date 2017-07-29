package pl.asie.charset.module.storage.barrels;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IOutputSupplierFactory;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nullable;

public class BarrelUpgradeOutputSupplier implements IOutputSupplier {
    private final TileEntityDayBarrel.Upgrade upgradeType;

    private BarrelUpgradeOutputSupplier(TileEntityDayBarrel.Upgrade upgradeType) {
        this.upgradeType = upgradeType;
    }

    @Override
    public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
        for (Ingredient i : matcher.getMatchedIngredients()) {
            ItemStack is = matcher.getStack(i);
            if (is.getItem() instanceof ItemDayBarrel || is.getItem() instanceof ItemMinecartDayBarrel) {
                is = is.copy();
                is.setCount(1);
                return TileEntityDayBarrel.addUpgrade(is, upgradeType);
            }
        }

        return null;
    }

    @Override
    public ItemStack getDefaultOutput() {
        return new ItemStack(CharsetStorageBarrels.barrelCartItem);
    }

    public static class Factory implements IOutputSupplierFactory {
        @Override
        public IOutputSupplier parse(JsonContext context, JsonObject json) {
            return new BarrelUpgradeOutputSupplier(TileEntityDayBarrel.Upgrade.valueOf(JsonUtils.getString(json, "upgrade")));
        }
    }
}
