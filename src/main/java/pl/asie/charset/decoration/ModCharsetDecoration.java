/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.decoration;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.decoration.poster.EntityPoster;
import pl.asie.charset.decoration.poster.ItemPoster;
import pl.asie.charset.decoration.scaffold.BlockScaffold;
import pl.asie.charset.decoration.scaffold.TileScaffold;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;

@Mod(modid = ModCharsetDecoration.MODID, name = ModCharsetDecoration.NAME, version = ModCharsetDecoration.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetDecoration {
	public static final String MODID = "charsetdecoration";
	public static final String NAME = "^";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.decoration.ProxyClient", serverSide = "pl.asie.charset.decoration.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetDecoration instance;
	public static Logger logger;

	public static Block scaffoldBlock;
	public static Item posterItem;

	private Configuration config;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetDecoration.MODID);
		config = new Configuration(ModCharsetLib.instance.getConfigFile("decoration.cfg"));

		scaffoldBlock = new BlockScaffold();
		ModCharsetLib.proxy.registerBlock(scaffoldBlock, "scaffold");
		ModCharsetLib.proxy.registerItemModel(scaffoldBlock, 0, "charsetdecoration:scaffold");

		posterItem = new ItemPoster();
		GameRegistry.register(posterItem.setRegistryName("poster"));
		ModCharsetLib.proxy.registerItemModel(posterItem, 0, "charsetdecoration:poster");

		proxy.preInit();
		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		EntityRegistry.registerModEntity(new ResourceLocation("charsetdecoration:poster"), EntityPoster.class, "charsetdecoration:poster", 1, this, 64, 3, true);
		GameRegistry.registerTileEntity(TileScaffold.class, "charsetdecoration:scaffold");

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(posterItem),
				"0",
				"-",
				"0",
				'-', "paper", '0', "slimeball"
		));
	}

	private void registerScaffoldRecipe(ItemStack log) {
		// todo: unify into some kind of wood registry
		// we do it this way to not register recipes for invalid logs
		InventoryCrafting plankCrafting = RecipeUtils.getCraftingInventory(3, 3);
		plankCrafting.setInventorySlotContents(0, log);
		IRecipe plankRecipe = RecipeUtils.findMatchingRecipe(plankCrafting, null);

		if (plankRecipe != null) {
			ItemStack plank = plankRecipe.getCraftingResult(plankCrafting);
			ItemStack scaffold = new ItemStack(scaffoldBlock);
			scaffold.setTagCompound(new NBTTagCompound());
			ItemUtils.writeToNBT(plank, scaffold.getTagCompound(), "plank");

			GameRegistry.addRecipe(new ShapedOreRecipe(scaffold,
					"ppp",
					" s ",
					"s s",
					's', "stickWood", 'p', plank
			));
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (ItemStack log : OreDictionary.getOres("logWood", false)) {
			try {
				if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
					for (int i = 0; i < (log.getItem() instanceof ItemBlock ? 16 : 128); i++) {
						ItemStack stack = new ItemStack(log.getItem(), 1, i);
						registerScaffoldRecipe(stack);
					}
				} else {
					registerScaffoldRecipe(log.copy());
				}
			} catch (Exception e) {

			}
		}
	}
}
