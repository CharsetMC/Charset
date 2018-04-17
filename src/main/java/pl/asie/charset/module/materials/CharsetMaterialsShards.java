/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.materials;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RegistryUtils;

import java.util.HashMap;
import java.util.Map;

@CharsetModule(
		name = "materials.shards",
		description = "Adds shards to glass.",
		dependencies = {"materials"},
		profile = ModuleProfile.STABLE
)
public class CharsetMaterialsShards {
	private static ItemGlassShard glassShardItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		glassShardItem = new ItemGlassShard();
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(glassShardItem, 0, "charset:shard#inventory_glass");
		for (int i = 1; i <= ItemGlassShard.MAX_SHARD; i++) {
			RegistryUtils.registerModel(glassShardItem, i, "charset:shard#inventory_stained_glass");
		}
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), glassShardItem, "shard");
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation("charset:glassShard"), new ItemStack(Blocks.GLASS), "gg", "gg", 'g', new ItemStack(glassShardItem, 1, 0)).setRegistryName(new ResourceLocation("charset:glassShard")));

		for (int i = 0; i < 16; i++) {
			event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation("charset:glassShard"), new ItemStack(Blocks.STAINED_GLASS, 1, i), "gg", "gg", 'g', new ItemStack(glassShardItem, 1, i + 1)).setRegistryName(new ResourceLocation("charset:glassShard_" + i)));
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		ItemMaterialRegistry imr = ItemMaterialRegistry.INSTANCE;
		ItemStack shard = new ItemStack(glassShardItem, 1, 0);

		OreDictionary.registerOre("shardGlass", new ItemStack(glassShardItem, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("shardGlassColorless", shard);

		for (ItemStack stack : OreDictionary.getOres("blockGlassColorless")) {
			imr.registerRelation(imr.getOrCreateMaterial(stack),
					imr.getOrCreateMaterial(shard), "shard", "block");
		}

		for (int i = 0; i < 16; i++) {
			ItemStack shardColored = new ItemStack(glassShardItem, 1, i + 1);
			for (ItemStack stack : OreDictionary.getOres(ColorUtils.getOreDictEntry("blockGlass", EnumDyeColor.byMetadata(i)))) {
				imr.registerRelation(imr.getOrCreateMaterial(stack),
						imr.getOrCreateMaterial(shardColored), "shard", "block");
			}

			OreDictionary.registerOre(ColorUtils.getOreDictEntry("shardGlass", EnumDyeColor.byMetadata(i)), shardColored);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(new ItemGlassShard.Color(), CharsetMaterialsShards.glassShardItem);
	}

	@SubscribeEvent
	public void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
		if (event.getState() == null) {
			return;
		}

		// WORKAROUND: Some mods seem to like event.getDrops() being null.
		// This is not what Forge does.

		if (event.getDrops() == null) {
			ModCharset.logger.error("Block " + event.getState().getBlock().getRegistryName() + " provides a null getDrops() list, against Forge's original method behaviour! This is a bug in the mod providing it!");
			return;
		}

		if (event.getDrops().size() > 0) {
			return;
		}

//		ItemStack pickBlockVariant = event.getState().getBlock().getItem(event.getWorld(), event.getPos(), event.getState());
		Block block = event.getState().getBlock();
		boolean isPane = false;
		int md = 0;

		if (block == Blocks.GLASS) {
			md = 0;
		} else if (block == Blocks.STAINED_GLASS) {
			md = 1 + block.getMetaFromState(event.getState());
		} else if (block == Blocks.GLASS_PANE) {
			isPane = true;
		} else if (block == Blocks.STAINED_GLASS_PANE) {
			isPane = true;
			md = 1 + block.getMetaFromState(event.getState());
		} else {
			return;
		}

		if (event.getDropChance() <= 0.0f) {
			event.setDropChance(1.0f);
		}

		if (isPane) {
			float rand = event.getWorld().rand.nextFloat();
			if (rand >= 0.5f) {
				event.getDrops().add(new ItemStack(glassShardItem, 1, md));
			}
		} else {
			int rand = event.getWorld().rand.nextInt(4) + 1;
			event.getDrops().add(new ItemStack(glassShardItem, rand, md));
		}
	}
}
