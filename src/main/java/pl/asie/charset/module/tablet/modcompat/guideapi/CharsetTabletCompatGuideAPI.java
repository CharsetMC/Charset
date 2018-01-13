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
			TabletAPI.INSTANCE.addBook(entry.getValue().getDisplayName(), "guideapi://" + entry.getKey().getResourceDomain() + "/" + entry.getKey().getResourcePath() + "/index");
		}
	}
}
