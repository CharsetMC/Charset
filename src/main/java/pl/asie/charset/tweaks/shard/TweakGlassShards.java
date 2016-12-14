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

package pl.asie.charset.tweaks.shard;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.tweaks.ModCharsetTweaks;
import pl.asie.charset.tweaks.Tweak;

public class TweakGlassShards extends Tweak {
	public static ItemShard shardItem;

	public TweakGlassShards() {
		super("additions", "glassShards", "Adds glass shards which drop from glass in a manner similar to glowstone dust.",
				!Loader.isModLoaded("glass_shards") /* to be nice to ljfa */);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public boolean preInit() {
		shardItem = new ItemShard();
		GameRegistry.register(shardItem.setRegistryName("shard"));

		ModCharsetLib.proxy.registerItemModel(shardItem, 0, "charsettweaks:shard");
		for (int i = 1; i <= ItemShard.MAX_SHARD; i++) {
			ModCharsetLib.proxy.registerItemModel(shardItem, i, "charsettweaks:shard#inventory_colored");
		}
		return true;
	}

	@Override
	public boolean init() {
		ModCharsetTweaks.proxy.initShardsTweakClient();
		MinecraftForge.EVENT_BUS.register(this);

		OreDictionary.registerOre("shardGlass", new ItemStack(shardItem, 1, OreDictionary.WILDCARD_VALUE));

		GameRegistry.addShapedRecipe(new ItemStack(Blocks.GLASS), "gg", "gg", 'g', new ItemStack(shardItem, 1, 0));
		OreDictionary.registerOre("shardGlassColorless", new ItemStack(shardItem, 1, 0));

		for (int i = 0; i < 16; i++) {
			GameRegistry.addShapedRecipe(new ItemStack(Blocks.STAINED_GLASS, 1, i), "gg", "gg", 'g', new ItemStack(shardItem, 1, i + 1));
			OreDictionary.registerOre(ColorUtils.getOreDictEntry("shardGlass", i), new ItemStack(shardItem, 1, i + 1));
		}
		return true;
	}

	@SubscribeEvent
	public void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
		if (event.getState() == null) {
			return;
		}

		// WORKAROUND: Some mods seem to like event.getDrops() being null.
		// This is not what Forge does.
		if (event.getDrops() == null) {
			ModCharsetLib.logger.error("Block " + event.getState().getBlock().getRegistryName() + " provides a null getDrops() list, against Forge's original method behaviour! This is a bug in the mod providing it!");
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
