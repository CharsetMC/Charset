package pl.asie.charset.lib.render.sprite;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextureAtlasSpriteCustom extends TextureAtlasSprite {
	private static final MethodHandle ANIMATION_METADATA_SETTER = MethodHandleHelper.findFieldSetter(TextureAtlasSprite.class, "animationMetadata", "field_110982_k");

	protected TextureAtlasSpriteCustom(String spriteName) {
		super(spriteName);
	}

	protected void addFrameTextureData(int width, int height, int[] pixels) {
		addFrameTextureData(width, height, pixels, null);
	}

	protected void addFrameTextureData(int width, int height, int[] pixels, @Nullable IResource resource) {
		AnimationMetadataSection section = resource != null ? resource.getMetadata("animation") : null;

		this.clearFramesTextureData();

		if (section == null) {
			this.setIconWidth(width);
			this.setIconHeight(height);

			int[][] pixelsCtr = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
			pixelsCtr[0] = pixels;
			framesTextureData.add(pixelsCtr);
		} else {
			this.setIconWidth(width);
			this.setIconHeight(width);

			int i = height / width;
			if (section.getFrameCount() > 0) {
				for (Integer index : section.getFrameIndexSet()) {
					if (index >= i) {
						throw new RuntimeException("invalid frameindex " + index);
					}

					while (framesTextureData.size() < index) {
						framesTextureData.add(null);
					}

					int[] data = new int[width * width];
					int[][] container = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
					System.arraycopy(pixels, width * width * index, data, 0, width * width);
					container[0] = data;

					framesTextureData.set(index, container);
				}
			} else {
				List<AnimationFrame> frames = new ArrayList<>(i);
				for (int index = 0; index < i; index++) {
					int[] data = new int[width * width];
					int[][] container = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
					System.arraycopy(pixels, width * width * index, data, 0, width * width);
					container[0] = data;

					framesTextureData.add(container);
					frames.add(new AnimationFrame(index, -1));
				}
				section = new AnimationMetadataSection(frames, width, height, section.getFrameTime(), section.isInterpolate());
			}

			try {
				ANIMATION_METADATA_SETTER.invokeExact((TextureAtlasSprite) this, section);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}
}
