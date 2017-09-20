/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tweaks;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlazedTerracotta;
import net.minecraft.block.BlockRailBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.charset.lib.handlers.ShiftScrollHandler;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.ArrayList;
import java.util.Collection;

@CharsetModule(
		name = "tweak.shiftScroll",
		description = "Enables shift-scrolling on vanilla blocks.",
		profile = ModuleProfile.TESTING
)
public class CharsetTweakShiftScroll {
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Blocks.WOOL));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Blocks.STAINED_GLASS));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Blocks.STAINED_GLASS_PANE));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Blocks.STAINED_HARDENED_CLAY));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Blocks.CONCRETE));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Blocks.CONCRETE_POWDER));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Items.BED));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(Items.DYE));

		/* ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.OreDictionaryGroup("plankWood"));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.OreDictionaryGroup("logWood")); */

		Multimap<String, Block> glazedTerracottas = LinkedHashMultimap.create();
		Collection<Block> rails = new ArrayList<>();
		Collection<Item> records = new ArrayList<>();

		for (Block b : ForgeRegistries.BLOCKS) {
			if (b instanceof BlockGlazedTerracotta) glazedTerracottas.put(b.getRegistryName().getResourceDomain(), b);
			else if (b instanceof BlockRailBase) rails.add(b);
		}

		for (Item i : ForgeRegistries.ITEMS) {
			if (i instanceof ItemRecord) records.add(i);
		}

		for (String domain : glazedTerracottas.keys()) {
			ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(glazedTerracottas.get(domain)));
		}
		/* ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(rails));
		ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(records)); */
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ShiftScrollHandler.INSTANCE.loadCustomRules();
	}
}
