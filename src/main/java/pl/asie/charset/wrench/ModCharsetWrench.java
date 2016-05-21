package pl.asie.charset.wrench;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.wires.logic.PartWireProvider;

@Mod(modid = ModCharsetWrench.MODID, name = ModCharsetWrench.NAME, version = ModCharsetWrench.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetWrench {
	public static final String MODID = "CharsetWrench";
	public static final String NAME = "/";
	public static final String VERSION = "@VERSION@";

	public static ItemWrench wrench;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MultipartRegistry.registerPartFactory(new PartWireProvider(), "charsetwires:wire");

		wrench = new ItemWrench();
		GameRegistry.register(wrench.setRegistryName("wrench"));
		ModCharsetLib.proxy.registerItemModel(wrench, 0, "charsetwrench:wrench");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wrench),
				" i ", " si", "i  ", 's', "stickWood", 'i', "ingotIron"));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
