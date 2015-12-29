package pl.asie.charset.lib.utils;

import java.io.IOException;

import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import pl.asie.charset.lib.ModCharsetLib;

public final class ClientUtils {
    public static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
    {
        public TextureAtlasSprite apply(ResourceLocation location)
        {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
        }
    };

    private ClientUtils() {

    }

    public static IModel getModel(ResourceLocation location) {
        try {
            IModel model = ModelLoaderRegistry.getModel(location);
            if (model == null) {
                ModCharsetLib.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
            }
            return model;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
