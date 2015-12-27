package pl.asie.charset.lib.utils;

import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class ClientUtils {
    public static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
    {
        public TextureAtlasSprite apply(ResourceLocation location)
        {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
        }
    };
}
