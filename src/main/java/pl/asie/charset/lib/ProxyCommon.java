package pl.asie.charset.lib;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ProxyCommon {
	public void registerItemModel(Item item, int meta, String name) {

	}

	public void registerItemModel(Block block, int meta, String name) {
		registerItemModel(Item.getItemFromBlock(block), meta, name);
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

    public boolean isClientThread() {
        return false;
    }

    public void addScheduledClientTask(Runnable runnable) {

    }
}
