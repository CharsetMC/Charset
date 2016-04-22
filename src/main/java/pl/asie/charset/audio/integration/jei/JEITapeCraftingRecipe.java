package pl.asie.charset.audio.integration.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.tape.ItemTape;
import pl.asie.charset.audio.tape.RecipeTape;

public class JEITapeCraftingRecipe extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	public static class Handler implements IRecipeHandler<RecipeTape> {

		@Nonnull
		@Override
		public Class<RecipeTape> getRecipeClass() {
			return RecipeTape.class;
		}

		@Nonnull
		@Override
		public String getRecipeCategoryUid() {
			return VanillaRecipeCategoryUid.CRAFTING;
		}

		@Nonnull
		@Override
		public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeTape recipe) {
			return new JEITapeCraftingRecipe();
		}

		@Override
		public boolean isRecipeValid(@Nonnull RecipeTape recipe) {
			return true;
		}
	}

	@Nonnull
	@Override
	public List getInputs() {
		Object[] inputs = new Object[9];

		List<String> mats = new ArrayList<String>();
		for (ItemTape.Material m : ItemTape.Material.values()) {
			if (OreDictionary.doesOreNameExist(m.oreDict)) {
				mats.add(m.oreDict);
			}
		}

		inputs[0] = mats;
		inputs[1] = mats;
		inputs[2] = mats;
		inputs[3] = new ItemStack(ModCharsetAudio.tapeReelItem, 1, OreDictionary.WILDCARD_VALUE);
		inputs[5] = inputs[3];
		inputs[6] = inputs[7] = inputs[8] = new ItemStack(Blocks.STONE_SLAB);

		return Arrays.asList(inputs);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(new ItemStack(ModCharsetAudio.tapeItem, 1, OreDictionary.WILDCARD_VALUE));
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}

}
