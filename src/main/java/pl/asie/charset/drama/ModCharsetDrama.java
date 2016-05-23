package pl.asie.charset.drama;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetDrama.MODID, name = ModCharsetDrama.NAME, version = ModCharsetDrama.VERSION,
		dependencies = ModCharsetLib.DEP_LIB, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetDrama {
	public static final String MODID = "CharsetDrama";
	public static final String NAME = "!";
	public static final String VERSION = "@VERSION@";

	@Mod.Instance(MODID)
	public static ModCharsetDrama instance;

	public static Logger logger;

	public static Item dramaInABottle;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetDrama.MODID);

		dramaInABottle = new ItemDramaInABottle();
		dramaInABottle.setUnlocalizedName("charset.dramaInABottle");
		dramaInABottle.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		dramaInABottle.setMaxStackSize(1);

		GameRegistry.register(dramaInABottle.setRegistryName("dramaInABottle"));
		ModCharsetLib.proxy.registerItemModel(dramaInABottle, 0, "charsetdrama:dramaInABottle");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		BrewingRecipeRegistry.addRecipe(new ItemStack(Items.POTIONITEM, 1, 16), "dyePink", new ItemStack(dramaInABottle));
	}
}
