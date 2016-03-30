package pl.asie.charset.gates;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.gates.render.GateRenderDefinitions;
import pl.asie.charset.gates.render.RendererGate;

/**
 * Created by asie on 12/27/15.
 */
public class ProxyClient extends ProxyCommon {
	private final Set<ResourceLocation> textures = new HashSet<ResourceLocation>();

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		for (String s : ModCharsetGates.gateParts.keySet()) {
			event.getModelRegistry().putObject(new ModelResourceLocation(s, "multipart"), RendererGate.INSTANCE);
			event.getModelRegistry().putObject(new ModelResourceLocation(s, "inventory"), RendererGate.INSTANCE);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		textures.clear();

		GateRenderDefinitions.INSTANCE.load("charsetgates:gatedefs/base.json", ModCharsetGates.gateDefintions);

		for (String s : ModCharsetGates.gateDefintions.keySet()) {
			ResourceLocation rs = new ResourceLocation(s);

			GateRenderDefinitions.Definition def = GateRenderDefinitions.INSTANCE.getGateDefinition(rs);
			for (IModel model : def.getAllModels()) {
				textures.addAll(model.getTextures());
			}
			for (GateRenderDefinitions.Layer layer : GateRenderDefinitions.INSTANCE.getGateDefinition(rs).layers) {
				if (layer.texture != null) {
					event.getMap().registerSprite(new ResourceLocation(layer.texture));
				}
			}
		}

		for (ResourceLocation r : textures) {
			event.getMap().registerSprite(r);
		}
	}
}
