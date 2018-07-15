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

package pl.asie.charset.module.tablet.modcompat.guideapi;

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.impl.Book;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.module.tablet.format.api.TabletAPI;

import java.util.Map;

@CharsetModule(
		name = "guideapi:tablet",
		profile = ModuleProfile.COMPAT,
		dependencies = {"mod:guideapi", "tablet"}
)
public class CharsetTabletCompatGuideAPI {
	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPostInit(FMLPostInitializationEvent event) {
		for (Map.Entry<ResourceLocation, Book> entry : GuideAPI.getBooks().entrySet()) {
			TabletAPI.INSTANCE.registerRouter(new RouterGuideAPI(entry.getKey(), entry.getValue()));
			TabletAPI.INSTANCE.addBook(entry.getValue().getDisplayName(), "guideapi://" + entry.getKey().getNamespace() + "/" + entry.getKey().getPath() + "/index");
		}
	}
}
