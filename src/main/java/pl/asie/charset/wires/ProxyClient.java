package pl.asie.charset.wires;

import net.minecraft.client.resources.model.ModelResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.wires.render.RendererWire;

/**
 * Created by asie on 12/5/15.
 */
public class ProxyClient extends ProxyCommon {
	public static RendererWire rendererWire = new RendererWire();

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.modelRegistry.putObject(new ModelResourceLocation("charsetwires:wire", "normal"), rendererWire);
		event.modelRegistry.putObject(new ModelResourceLocation("charsetwires:wire", "inventory"), rendererWire);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		rendererWire.loadTextures(event.map);
	}
}
