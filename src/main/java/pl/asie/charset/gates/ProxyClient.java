package pl.asie.charset.gates;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.gates.render.GateRenderDefinitions;
import pl.asie.charset.gates.render.RendererGate;
import pl.asie.charset.lib.ModCharsetLib;

/**
 * Created by asie on 12/27/15.
 */
public class ProxyClient extends ProxyCommon {
    public static IRetexturableModel gateModel, gateLayerModel;
    public static IModel[] gateTorchModel = new IModel[2];

    private final Set<ResourceLocation> textures = new HashSet<ResourceLocation>();

    private IModel getModel(ResourceLocation location) {
        try {
            IModel model = ModelLoaderRegistry.getModel(location);
            if (model != null) {
                textures.addAll(model.getTextures());
            } else {
                ModCharsetLib.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
            }
            return model;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        for (String s : ModCharsetGates.gateParts.keySet()) {
            event.modelRegistry.putObject(new ModelResourceLocation(s, "multipart"), RendererGate.INSTANCE);
            event.modelRegistry.putObject(new ModelResourceLocation(s, "inventory"), RendererGate.INSTANCE);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        textures.clear();

        gateModel = (IRetexturableModel) getModel(new ResourceLocation("charsetgates:block/gate_base"));
        gateLayerModel = (IRetexturableModel) getModel(new ResourceLocation("charsetgates:block/gate_layer"));
        gateTorchModel[0] = getModel(new ResourceLocation("charsetgates:block/gate_torch_off"));
        gateTorchModel[1] = getModel(new ResourceLocation("charsetgates:block/gate_torch_on"));

        GateRenderDefinitions.INSTANCE.load("charsetgates:gatedefs/base.json", ModCharsetGates.gateDefintions);

        for (String s : ModCharsetGates.gateTextures.keySet()) {
            event.map.registerSprite(ModCharsetGates.gateTextures.get(s));
            for (GateRenderDefinitions.Layer layer : GateRenderDefinitions.INSTANCE.getGateDefinition(s).layers) {
                if (layer.texture != null) {
                    event.map.registerSprite(new ResourceLocation(layer.texture));
                }
            }
        }

        for (ResourceLocation r : textures) {
            event.map.registerSprite(r);
        }
    }
}
