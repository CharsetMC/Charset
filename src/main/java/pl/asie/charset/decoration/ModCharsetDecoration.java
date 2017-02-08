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
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import pl.asie.charset.decoration.scaffold.BlockScaffold;
import pl.asie.charset.decoration.scaffold.ItemScaffold;
import pl.asie.charset.decoration.scaffold.TileScaffold;
import pl.asie.charset.lib.ModCharsetBase;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;

@Mod(modid = ModCharsetDecoration.MODID, name = ModCharsetDecoration.NAME, version = ModCharsetDecoration.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetDecoration extends ModCharsetBase {
	public static final String MODID = "charsetdecoration";
	public static final String NAME = "^";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.decoration.ProxyClient", serverSide = "pl.asie.charset.decoration.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetDecoration instance;

	public static Block scaffoldBlock;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!ForgeModContainer.fullBoundingBoxLadders) {
			logger.warn("To make Charset scaffolds work better, we recommend enabling fullBoundingBoxLadders in forge.cfg.");
		}

		scaffoldBlock = new BlockScaffold();
		ModCharsetLib.proxy.registerBlock(scaffoldBlock, new ItemScaffold(scaffoldBlock), "scaffold");
		ModCharsetLib.proxy.registerItemModel(scaffoldBlock, 0, "charsetdecoration:scaffold");

		proxy.preInit();
		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (scaffoldBlock != null) {
			GameRegistry.registerTileEntity(TileScaffold.class, "charsetdecoration:scaffold");
		}
	}

	private void registerScaffoldRecipe(ItemMaterial plankMaterial) {
		ItemStack plank = plankMaterial.getStack();
		ItemStack scaffold = BlockScaffold.createStack(plankMaterial, 4);

		BlockScaffold.PLANKS.add(plankMaterial);

		GameRegistry.addRecipe(new ShapedOreRecipe(scaffold,
				"ppp",
				" s ",
				"s s",
				's', "stickWood", 'p', plank
		));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (scaffoldBlock != null) {
			for (ItemMaterial plankMaterial : ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("plank", "block")) {
				registerScaffoldRecipe(plankMaterial);
			}
		}
	}
}
