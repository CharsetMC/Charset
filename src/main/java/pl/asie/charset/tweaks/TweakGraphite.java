package pl.asie.charset.tweaks;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import pl.asie.charset.lib.ModCharsetLib;

/**
 * Created by asie on 12/4/15.
 */
public class TweakGraphite extends Tweak {
	private Item graphite;

	public TweakGraphite() {
		super("additions", "graphite", "Adds a graphite item crafted from charcoal which acts as black dye.", true);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public void preInit() {
		graphite = new Item().setCreativeTab(ModCharsetLib.CREATIVE_TAB).setUnlocalizedName("charset.graphite");
		GameRegistry.registerItem(graphite, "graphite");

		ModCharsetLib.proxy.registerItemModel(graphite, 0, "charsettweaks:graphite");
	}

	@Override
	public void init() {
		OreDictionary.registerOre("dyeBlack", graphite);
		GameRegistry.addShapelessRecipe(new ItemStack(graphite, 2, 0), new ItemStack(Items.coal, 1, 1));
	}
}
