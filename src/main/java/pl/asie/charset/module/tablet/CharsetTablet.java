package pl.asie.charset.module.tablet;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.TabletAPI;
import pl.asie.charset.module.tablet.format.commands.CommandImg;
import pl.asie.charset.module.tablet.format.commands.CommandItem;
import pl.asie.charset.module.tablet.format.commands.CommandURL;
import pl.asie.charset.module.tablet.format.routers.RouterModDocumentation;
import pl.asie.charset.module.tablet.format.words.*;
import pl.asie.charset.module.tablet.format.words.minecraft.*;

@CharsetModule(
		name = "tablet",
		description = "The Tablet, providing documentation for Charset and other mods!",
		profile = ModuleProfile.INDEV
)
public class CharsetTablet {
	public static ItemTablet itemTablet;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		ICommand spaceCommand = ((typesetter, tokenizer) -> typesetter.write(new WordText(" ")));
		ICommand newlineCommand = ((typesetter, tokenizer) -> typesetter.write(new WordNewline()));

		TabletAPI.INSTANCE.registerRouter(new RouterModDocumentation("charset"));

		TabletAPI.INSTANCE.registerCommand("\\", spaceCommand);
		TabletAPI.INSTANCE.registerCommand("\\ ", spaceCommand);
		TabletAPI.INSTANCE.registerCommand("\\nl", newlineCommand);
		TabletAPI.INSTANCE.registerCommand("\\p", newlineCommand);
		TabletAPI.INSTANCE.registerCommand("\\\\", (((typesetter, tokenizer) -> typesetter.write(new WordText("\\")))));
		TabletAPI.INSTANCE.registerCommand("\\-", (((typesetter, tokenizer) -> typesetter.write(new WordBullet()))));

		TabletAPI.INSTANCE.registerCommand("\\local", ((typesetter, tokenizer) -> typesetter.write(new WordTextLocalized(tokenizer.getParameter("text")))));
		TabletAPI.INSTANCE.registerCommand("\\title", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.ITALIC, StyleFormat.UNDERLINE);
			typesetter.write(new WordText(tokenizer.getParameter("text"), 2.0f));
			typesetter.popStyle(2);
		}));
		TabletAPI.INSTANCE.registerCommand("\\header", ((typesetter, tokenizer) -> {
			typesetter.pushStyle(StyleFormat.BOLD, StyleFormat.UNDERLINE);
			typesetter.write(new WordText(tokenizer.getParameter("text"), 1.0f));
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

		TabletAPI.INSTANCE.registerCommand("\\img", new CommandImg());
		TabletAPI.INSTANCE.registerCommand("\\item", new CommandItem());
		TabletAPI.INSTANCE.registerCommand("\\url", new CommandURL());

		TabletAPI.INSTANCE.registerPrinterMinecraft(WordBullet.class, new WordPrinterMCBullet());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordImage.class, new WordPrinterMCImage());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordItem.class, new WordPrinterMCItem());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordNewline.class, new WordPrinterMCNewline());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordText.class, new WordPrinterMCText());
		TabletAPI.INSTANCE.registerPrinterMinecraft(WordURL.class, new WordPrinterMCURL());
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
