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

package pl.asie.charset.tweaks;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import pl.asie.charset.lib.ModCharsetLib;

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
	public boolean preInit() {
		graphite = new Item().setCreativeTab(ModCharsetLib.CREATIVE_TAB).setUnlocalizedName("charset.graphite");
		GameRegistry.register(graphite.setRegistryName("graphite"));

		ModCharsetLib.proxy.registerItemModel(graphite, 0, "charsettweaks:graphite");
		return true;
	}

	@Override
	public boolean init() {
		OreDictionary.registerOre("dyeBlack", graphite);
		GameRegistry.addShapelessRecipe(new ItemStack(graphite, 2, 0), new ItemStack(Items.COAL, 1, 1));
		return true;
	}
}
