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

package pl.asie.charset.lib.config;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.ModuleLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ConfigGui extends GuiConfig {
	public ConfigGui(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(), ModCharset.MODID, "Charset", false, false, I18n.format("config.charset.title"));
	}

	public static List<IConfigElement> generateList(ConfigCategory category) {
		List<IConfigElement> list = new ArrayList<>();
		for (Property prop : category.values()) {
			list.add(new ConfigElement(prop));
		}
		return list;
	}

	public static List<IConfigElement> generateList(Configuration config) {
		List<IConfigElement> list = new ArrayList<>();
		for (String name : config.getCategoryNames()) {
			ConfigCategory category = config.getCategory(name);
			list.add(new DummyConfigElement.DummyCategoryElement(category.getName(), category.getLanguagekey(), generateList(category)));
		}
		return list;
	}

	private static List<IConfigElement> getSubConfigElements(Configuration config, String category, Function<Property, IConfigElement> creator) {
		List<IConfigElement> list = new ArrayList<>();
		for (Property prop : config.getCategory(category).values()) {
			list.add(creator.apply(prop));
		}
		return list;
	}

	private static List<IConfigElement> getConfigElements() {
		Configuration config = ModCharset.configModules;
		List<IConfigElement> list = Lists.newArrayList(
				new ConfigElement(config.getCategory("general").get("profile")),
				new DummyConfigElement.DummyCategoryElement("categories", "config.charset.categories.name", getSubConfigElements(config, "categories", ConfigElement::new)),
				new DummyConfigElement.DummyCategoryElement("overrides", "config.charset.overrides.name", getSubConfigElements(config, "overrides", ConfigElement::new))
		);

		List<String> modulesWithConfigs = Lists.newArrayList(ModuleLoader.moduleConfigs.keySet());
		Collections.sort(modulesWithConfigs);

		for (String s : modulesWithConfigs) {
			List<IConfigElement> clist = null;
			String clsName = ModuleLoader.moduleGuiClasses.get(s);
			if (clsName != null && clsName.length() > 0) {
				try {
					clist = ((ICharsetModuleConfigGui) Class.forName(clsName).newInstance()).createConfigElements();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (clist == null) {
				clist = generateList(ModuleLoader.moduleConfigs.get(s));
			}

			list.add(new DummyConfigElement.DummyCategoryElement(s, "config.charset." + s + ".name", clist));
		}

		return list;
	}
}
