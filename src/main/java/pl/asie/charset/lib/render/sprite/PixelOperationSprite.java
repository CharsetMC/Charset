package pl.asie.charset.lib.render.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public abstract class PixelOperationSprite extends TextureAtlasSprite {
    public static class Multiply extends PixelOperationSprite {
        private final int color;

        public Multiply(String entry, ResourceLocation location, int color) {
            super(entry, location);
            this.color = color;
        }

        @Override
        public int apply(int x, int y, int value) {
            return RenderUtils.multiplyColor(value, color);
        }
    }

    private final ResourceLocation location;

    protected PixelOperationSprite(String entry, ResourceLocation location) {
        super(entry);
        this.location = location;
    }

    public abstract int apply(int x, int y, int value);

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
        BufferedImage image = RenderUtils.getTextureImage(location);
        if (image == null) {
            return false;
        }

        setIconWidth(image.getWidth());
        setIconHeight(image.getHeight());

        int[][] pixels = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][];
        pixels[0] = new int[image.getWidth() * image.getHeight()];

        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels[0], 0, image.getWidth());
        for (int i = 0; i < pixels[0].length; i++) {
            pixels[0][i] = apply(i % image.getWidth(), i / image.getWidth(), pixels[0][i]);
        }

        this.clearFramesTextureData();
        this.framesTextureData.add(pixels);

        return false;
    }
}
