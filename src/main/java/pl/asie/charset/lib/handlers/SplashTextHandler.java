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

package pl.asie.charset.lib.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashTextHandler {
	private static final ResourceLocation SPLASH_TEXTS = new ResourceLocation("texts/splashes.txt");
	private static final ResourceLocation EXTRA_SPLASH_TEXTS = new ResourceLocation("charset:texts/extra_splashes.txt");

	public void addTexts(List<String> splashes, ResourceLocation loc) {
		IResource resource = null;

		try {
			resource = Minecraft.getMinecraft().getResourceManager().getResource(loc);
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
			String s;

			while ((s = bufferedreader.readLine()) != null) {
				if (s.charAt(0) == '#')
					continue;

				s = s.trim();

				if (!s.isEmpty()) {
					splashes.add(s);
				}
			}
		} catch (IOException e) {

		}
		finally {
			IOUtils.closeQuietly(resource);
		}
	}

	public String getSplashText(List<String> splashes) {
		Random rand = new Random();

		if (!splashes.isEmpty()) {
			while (true) {
				String splashText = splashes.get(rand.nextInt(splashes.size()));

				if (splashText.hashCode() != 125780783) // "This message will never appear on the splash screen, isn't that weird?".hashCode()
					return splashText;
			}
		} else {
			return "?";
		}
	}

	@SubscribeEvent
	public void customSplashes(GuiScreenEvent.InitGuiEvent.Pre event) {
		if (event.getGui() instanceof GuiMainMenu) {
			GuiMainMenu menu = (GuiMainMenu) event.getGui();
			Field splashTextField = ReflectionHelper.findField(GuiMainMenu.class, "splashText", "field_73975_c");
			List<String> splashes = new ArrayList<>();
			addTexts(splashes, SPLASH_TEXTS);
			addTexts(splashes, EXTRA_SPLASH_TEXTS);
			try {
				splashTextField.set(menu, getSplashText(splashes));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
