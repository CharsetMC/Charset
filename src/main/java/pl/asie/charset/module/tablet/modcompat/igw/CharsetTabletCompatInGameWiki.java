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
