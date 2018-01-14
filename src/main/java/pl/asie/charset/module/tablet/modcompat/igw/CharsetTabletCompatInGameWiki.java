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

package pl.asie.charset.module.tablet.modcompat.igw;

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.impl.Book;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ModPathIterator;
import pl.asie.charset.module.tablet.format.api.TabletAPI;
import pl.asie.charset.module.tablet.modcompat.guideapi.RouterGuideAPI;

import java.nio.file.Path;
import java.util.Map;

@CharsetModule(
		name = "igw-mod:tablet",
		profile = ModuleProfile.COMPAT,
		dependencies = {"tablet"}
)
public class CharsetTabletCompatInGameWiki {
	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPostInit(FMLPostInitializationEvent event) {
		// HAYO
		for (Pair<String, Path> pair : ModPathIterator.getValidPaths("assets/%1/wiki")) {
			if ("igwmod".equals(pair.getLeft())) {
				RouterIGW igw = new RouterIGW("minecraft", pair.getRight().resolve("../../minecraft/wiki"));
				TabletAPI.INSTANCE.registerRouter(igw);
			}

			RouterIGW igw = new RouterIGW(pair.getLeft(), pair.getRight());
			TabletAPI.INSTANCE.registerRouter(igw);
			if (!("igwmod".equals(pair.getLeft()))) {
				TabletAPI.INSTANCE.addBook(igw.getFriendlyName(), "igw://" + pair.getLeft() + "/index");
			}
		}
	}
}
