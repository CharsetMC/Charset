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

package pl.asie.charset.misc.shards;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "misc.shards",
		description = "Adds glowstone-esque shards to glass"
)
public class CharsetMiscShards {
	public static ItemShard shardItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RegistryUtils.register(shardItem = new ItemShard(), "shard");

		RegistryUtils.registerModel(shardItem, 0, "charset:shard");
		for (int i = 1; i <= ItemShard.MAX_SHARD; i++) {
			RegistryUtils.registerModel(shardItem, i, "charset:shard#inventory_colored");
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		OreDictionary.registerOre("shardGlass", new ItemStack(shardItem, 1, OreDictionary.WILDCARD_VALUE));

		GameRegistry.addShapedRecipe(new ItemStack(Blocks.GLASS), "gg", "gg", 'g', new ItemStack(shardItem, 1, 0));
		OreDictionary.registerOre("shardGlassColorless", new ItemStack(shardItem, 1, 0));

		for (int i = 0; i < 16; i++) {
			GameRegistry.addShapedRecipe(new ItemStack(Blocks.STAINED_GLASS, 1, i), "gg", "gg", 'g', new ItemStack(shardItem, 1, i + 1));
			OreDictionary.registerOre(ColorUtils.getOreDictEntry("shardGlass", EnumDyeColor.byMetadata(i)), new ItemStack(shardItem, 1, i + 1));
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemShard.Color(), CharsetMiscShards.shardItem);
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
				event.getDrops().add(new ItemStack(shardItem, 1, md));
			}
		} else {
			int rand = event.getWorld().rand.nextInt(4) + 1;
			event.getDrops().add(new ItemStack(shardItem, rand, md));
		}
	}
}
