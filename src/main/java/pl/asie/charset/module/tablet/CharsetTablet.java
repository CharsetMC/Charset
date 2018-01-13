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

package pl.asie.charset.module.tablet;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.TabletAPI;
import pl.asie.charset.module.tablet.format.commands.*;
import pl.asie.charset.module.tablet.format.routers.RouterIndex;
import pl.asie.charset.module.tablet.format.routers.RouterMediaWiki;
import pl.asie.charset.module.tablet.format.routers.RouterModDocumentation;
import pl.asie.charset.module.tablet.format.routers.RouterSearch;
import pl.asie.charset.module.tablet.format.words.*;
import pl.asie.charset.module.tablet.format.words.minecraft.*;

@CharsetModule(
		name = "tablet",
		description = "The Tablet, providing documentation for Charset and other mods!",
		profile = ModuleProfile.TESTING
)
public class CharsetTablet {
	public static ItemTablet itemTablet;

	@CharsetModule.SidedProxy(clientSide = "pl.asie.charset.module.tablet.ProxyClient", serverSide = "pl.asie.charset.module.tablet.ProxyCommon")
	public static ProxyCommon proxy;

	@CharsetModule.Configuration
	public static Configuration config;

	public static boolean allowRemoteLookups;

	@Mod.EventHandler
	public void onLoadConfig(CharsetLoadConfigEvent event) {
		allowRemoteLookups = config.getBoolean("allowRemoteLookups", "general", true, "Should remote lookups be allowed?");;
	}

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onInitClient(FMLInitializationEvent event) {
		ICommand spaceCommand = ((typesetter, tokenizer) -> typesetter.write(new WordText(" ")));

		TabletAPI.INSTANCE.registerRouter(new RouterIndex());
		TabletAPI.INSTANCE.registerRouter(new RouterSearch());
		TabletAPI.INSTANCE.registerRouter(new RouterModDocumentation("charset", "Book of Charset"));

		if (allowRemoteLookups) {
			TabletAPI.INSTANCE.registerRouter(new RouterMediaWiki("gamepedia", "Gamepedia", "ftb.gamepedia.com", "minecraft.gamepedia.com"));
			if (Loader.isModLoaded("mekanism")) {
				TabletAPI.INSTANCE.registerRouter(new RouterMediaWiki("mekanism", "Mekanism Wiki", "wiki.aidancbrady.com/w"));
			}
		}

		TabletAPI.INSTANCE.registerCommand("\\", spaceCommand);
		TabletAPI.INSTANCE.registerCommand("\\ ", spaceCommand);
		TabletAPI.INSTANCE.registerCommand("\\nl", ((typesetter, tokenizer) -> typesetter.write(new WordNewline(1))));
		TabletAPI.INSTANCE.registerCommand("\\p", ((typesetter, tokenizer) -> typesetter.write(new WordNewline(2))));
		TabletAPI.INSTANCE.registerCommand("\\\\", (((typesetter, tokenizer) -> typesetter.write(new WordText("\\")))));
		TabletAPI.INSTANCE.registerCommand("\\-", (((typesetter, tokenizer) -> {
			String padS = tokenizer.getOptionalParameter();
			typesetter.write(new WordBullet(padS != null ? Integer.parseInt(padS) : 0));
		})));

		TabletAPI.INSTANCE.registerCommand("\\scale", ((typesetter, tokenizer) -> {
			String scaleS = tokenizer.getParameter("\\scale scaling amount");
			String content = tokenizer.getParameter("\\scale content");

			typesetter.pushStyle(new StyleScale(Float.parseFloat(scaleS)));
			typesetter.write(content);
			typesetter.popStyle(1);
		}));

		TabletAPI.INSTANCE.registerCommand("\\local", ((typesetter, tokenizer) -> typesetter.write(new WordTextLocalized(tokenizer.getParameter("text")))));
		TabletAPI.INSTANCE.registerCommand("\\title", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.ITALIC, StyleFormat.UNDERLINE, new StyleScale(2.0f));
			typesetter.write(tokenizer.getParameter("text"));
			typesetter.popStyle(3);
		}));
		TabletAPI.INSTANCE.registerCommand("\\header", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.BOLD, StyleFormat.UNDERLINE);
			typesetter.write(tokenizer.getParameter("text"));
			typesetter.popStyle(2);
		}));

		TabletAPI.INSTANCE.registerCommand("\\b", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.BOLD);
			typesetter.write(tokenizer.getParameter("text"));
			typesetter.popStyle();
		}));
		TabletAPI.INSTANCE.registerCommand("\\i", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.ITALIC);
			typesetter.write(tokenizer.getParameter("text"));
			typesetter.popStyle();
		}));
		TabletAPI.INSTANCE.registerCommand("\\u", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.UNDERLINE);
			typesetter.write(tokenizer.getParameter("text"));
			typesetter.popStyle();
		}));
		TabletAPI.INSTANCE.registerCommand("\\del", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.STRIKETHROUGH);
			typesetter.write(tokenizer.getParameter("text"));
			typesetter.popStyle();
		}));

		TabletAPI.INSTANCE.registerCommand("\\checkmods", new CommandCheckMods());
		TabletAPI.INSTANCE.registerCommand("\\img", new CommandImg());
		TabletAPI.INSTANCE.registerCommand("\\item", new CommandItem());
		TabletAPI.INSTANCE.registerCommand("\\url", new CommandURL());
		TabletAPI.INSTANCE.registerCommand("\\urlmissing", new CommandURLMissing());

		TabletAPI.INSTANCE.registerPrinterMinecraft(WordBullet.class, new WordPrinterMCBullet());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordImage.class, new WordPrinterMCImage());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordItem.class, new WordPrinterMCItem());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordNewline.class, new WordPrinterMCNewline());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordText.class, new WordPrinterMCText());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordURL.class, new WordPrinterMCURL());

		TabletAPI.INSTANCE.addBook("Book of Charset", "mod://charset/index");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemTablet = new ItemTablet(), "tablet");
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemTablet,0, "charset:tablet");
	}
}
