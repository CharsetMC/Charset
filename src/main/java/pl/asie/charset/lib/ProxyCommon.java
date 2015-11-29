package pl.asie.charset.lib;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ProxyCommon {
	public void registerItemModels() {

	}

	public void registerBlock(Block block, String name) {
		GameRegistry.registerBlock(block, name);
		block.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	public void registerRecipeShaped(ItemStack output, Object... recipe) {
		GameRegistry.addRecipe(new ShapedOreRecipe(output, recipe));
	}

	public void registerRecipeShapeless(ItemStack output, Object... recipe) {
		GameRegistry.addRecipe(new ShapelessOreRecipe(output, recipe));
	}

	public World getLocalWorld(int dim) {
		return DimensionManager.getWorld(dim);
	}
}
