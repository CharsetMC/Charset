package pl.asie.charset.lib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpritesheetFactory {
    private final ResourceLocation location;
    private final int width, height;

    protected static class SliceSprite extends TextureAtlasSprite {
        private final ResourceLocation location;
        private final int x, y, width, height;
        private BufferedImage sheet;

        protected SliceSprite(ResourceLocation loc, ResourceLocation location, int i, int width, int height) {
            super(loc.toString());
            this.location = location;
            this.width = width;
            this.height = height;
            this.x = i % width;
            this.y = i / width;
        }

        @Override
        public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
            return true;
        }

        @Override
        public boolean load(IResourceManager manager, ResourceLocation loc) {
            if (sheet == null) {
                try {
                    ResourceLocation pngLocation = new ResourceLocation(location.getResourceDomain(), String.format("%s/%s%s", new Object[] {"textures", location.getResourcePath(), ".png"}));
                    IResource resource = manager.getResource(pngLocation);
                    sheet = TextureUtil.readBufferedImage(resource.getInputStream());
                    if (sheet == null) {
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            int pieceWidth = sheet.getWidth() / width;
            int pieceHeight = sheet.getHeight() / height;

            setIconWidth(pieceWidth);
            setIconHeight(pieceHeight);

            int[][] pixels = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][];
            pixels[0] = new int[pieceWidth * pieceHeight];

            sheet.getRGB(pieceWidth * x, pieceHeight * y, pieceWidth, pieceHeight, pixels[0], 0, pieceWidth);

            this.clearFramesTextureData();
            this.framesTextureData.add(pixels);

            return false;
        }
    }

    private SpritesheetFactory(ResourceLocation location, int width, int height) {
        this.location = location;
        this.width = width;
        this.height = height;
    }

    public static TextureAtlasSprite[] register(TextureMap map, ResourceLocation location, int width, int height) {
        SpritesheetFactory factory = new SpritesheetFactory(location, width, height);
        return factory.register(map);
    }

    public TextureAtlasSprite[] register(TextureMap map) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[width * height];
        for (int i = 0; i < sprites.length; i++) {
            String s = String.format(location.toString() + "#%d", i);
            sprites[i] = new SliceSprite(new ResourceLocation(s), location, i, width, height);
            map.setTextureEntry(s, sprites[i]);
        }
        return sprites;
    }
}
