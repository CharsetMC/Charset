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

package pl.asie.charset.module.tweaks.voidgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlazedTerracotta;
import net.minecraft.block.BlockRailBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.handlers.ShiftScrollHandler;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.ArrayList;
import java.util.Collection;

@CharsetModule(
		name = "tweak.voidGenerator",
		description = "Void overworld.",
		isDefault = false,
		profile = ModuleProfile.TESTING
)
public class CharsetTweakVoidGen {
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		DimensionManager.unregisterDimension(0);
		DimensionManager.registerDimension(0,
				DimensionType.register("overworld", "", 0, WorldProviderCharsetVoid.class, true)
		);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().provider instanceof WorldProviderCharsetVoid) {
			event.getWorld().provider.setCloudRenderer(new RenderHandlerDummy());
		}
	}
}
