package pl.asie.charset.lib.misc;

import com.google.common.collect.Lists;
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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashTextHandler {
	private static final ResourceLocation SPLASH_TEXTS = new ResourceLocation("texts/splashes.txt");
	private static final ResourceLocation EXTRA_SPLASH_TEXTS = new ResourceLocation("charsetlib:texts/extra_splashes.txt");

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
