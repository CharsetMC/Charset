package pl.asie.charset.lib.recipe;

import gnu.trove.set.TCharSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;

public abstract class IngredientCharset extends Ingredient {
    protected boolean mustMatch;
    private IntList matchingStacksPacked;

    protected IngredientCharset(int p_i9_1_) {
        super(p_i9_1_);
    }

    public IngredientCharset requireMatches() {
        mustMatch = true;
        return this;
    }

    @Nullable
    public TCharSet getDependencies() {
        return null;
    }

    public void addDependency(char c, Ingredient i) {

    }

    public boolean mustIteratePermutations() {
        return getDependencies() != null;
    }

    public boolean apply(IngredientMatcher matcher, ItemStack stack) {
        return apply(stack);
    }

    public void applyToStack(ItemStack stack, ItemStack source) {

    }

    public boolean areItemStacksMatched(ItemStack a, ItemStack b) {
        return !mustMatch || ItemUtils.canMerge(a, b);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IntList getValidItemStacksPacked() {
        if(this.matchingStacksPacked == null) {
            ItemStack[] matchingStacks = getMatchingStacks();
            this.matchingStacksPacked = new IntArrayList(matchingStacks.length);
            ItemStack[] var1 = matchingStacks;
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                ItemStack itemstack = var1[var3];
                this.matchingStacksPacked.add(RecipeItemHelper.pack(itemstack));
            }

            this.matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.matchingStacksPacked;
    }
}
