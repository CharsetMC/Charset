/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.utils;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.module.misc.scaffold.ModelScaffold;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class RenderUtils {
	public enum AveragingMode {
		FULL,
		H_EDGES_ONLY,
		V_EDGES_ONLY,
	};

	private static RenderItem renderItem;

	private RenderUtils() {

	}

	public static BakedQuad createQuad(Vector3f from, Vector3f to, @Nonnull EnumFacing facing,
									   TextureAtlasSprite sprite, int tintIndex) {
		Vector3f fFrom = new Vector3f(from);
		Vector3f fTo = new Vector3f(to);
		EnumFacing.AxisDirection facingDir = facing.getAxisDirection();
		switch (facing.getAxis()) {
			case X:
				fFrom.x = fTo.x = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.x : from.x;
				break;
			case Y:
				fFrom.y = fTo.y = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.y : from.y;
				break;
			case Z:
				fFrom.z = fTo.z = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.z : from.z;
				break;
		}

		return CharsetFaceBakery.INSTANCE.makeBakedQuad(fFrom, fTo, tintIndex, sprite, facing, ModelRotation.X0_Y0, true);
	}

	public static int getAverageColor(TextureAtlasSprite sprite, AveragingMode mode) {
		if (sprite == null) {
			// Huh.
			return 0xFFFF00FF;
		}

		int pixelCount = 0;
		int[] data = sprite.getFrameTextureData(0)[0];
		long avgColorB = 0, avgColorG = 0, avgColorR = 0;
		switch (mode) {
			case FULL:
				for (int j = 0; j < sprite.getIconHeight(); j++) {
					for (int i = 0; i < sprite.getIconWidth(); i++) {
						int c = data[j * sprite.getIconWidth() + i];
						if (((c >> 24) & 0xFF) != 0x00) {
							avgColorB += (c & 0xFF)*(c & 0xFF);
							avgColorG += ((c >> 8) & 0xFF)*((c >> 8) & 0xFF);
							avgColorR += ((c >> 16) & 0xFF)*((c >> 16) & 0xFF);
							pixelCount++;
						}
					}
				}
				break;
			case H_EDGES_ONLY:
				for (int j = 0; j < 2; j++) {
					for (int i = 0; i < sprite.getIconHeight(); i++) {
						int c = data[i * sprite.getIconWidth() + (j > 0 ? sprite.getIconWidth() - 1 : 0)];
						if (((c >> 24) & 0xFF) != 0x00) {
							avgColorB += (c & 0xFF)*(c & 0xFF);
							avgColorG += ((c >> 8) & 0xFF)*((c >> 8) & 0xFF);
							avgColorR += ((c >> 16) & 0xFF)*((c >> 16) & 0xFF);
							pixelCount++;
						}
					}
				}
				break;
			case V_EDGES_ONLY:
				for (int j = 0; j < 2; j++) {
					for (int i = 0; i < sprite.getIconWidth(); i++) {
						int c = data[j > 0 ? (data.length - 1 - i) : i];
						if (((c >> 24) & 0xFF) != 0x00) {
							avgColorB += (c & 0xFF)*(c & 0xFF);
							avgColorG += ((c >> 8) & 0xFF)*((c >> 8) & 0xFF);
							avgColorR += ((c >> 16) & 0xFF)*((c >> 16) & 0xFF);
							pixelCount++;
						}
					}
				}
				break;
		}
		if (pixelCount > 0) {
			return 0xFF000000
				| ((Math.min(255, (int) (Math.sqrt(avgColorB / pixelCount))) & 0xFF))
				| ((Math.min(255, (int) (Math.sqrt(avgColorG / pixelCount))) & 0xFF) << 8)
				| ((Math.min(255, (int) (Math.sqrt(avgColorR / pixelCount))) & 0xFF) << 16);
		} else {
			return 0xFFFF00FF;
		}
	}

	public static BufferedImage getTextureImage(TextureAtlasSprite sprite) {
		if (sprite.getFrameCount() > 0) {
			int[][] dataM = sprite.getFrameTextureData(0);
			if (dataM.length > 0) {
				int[] data = dataM[0];
				BufferedImage image = new BufferedImage(sprite.getIconWidth(), sprite.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
				image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
				return image;
			}
		}

		return null;
	}

	public static ResourceLocation toTextureFilePath(ResourceLocation location) {
		ResourceLocation pngLocation = new ResourceLocation(location.getNamespace(), String.format("%s/%s%s", "textures", location.getPath(), ".png"));
		return pngLocation;
	}

	public static BufferedImage getTextureImage(ResourceLocation location, @Nullable Function<ResourceLocation, TextureAtlasSprite> getter) {
		if (getter != null) {
			TextureAtlasSprite sprite = getter.apply(location);
			if (sprite != null && !sprite.getIconName().equals("missingno") && sprite.getFrameCount() > 0 && sprite.getIconWidth() > 0 && sprite.getIconHeight() > 0) {
				int width, height;
				try {
					int frameSize = sprite.getIconWidth() * sprite.getIconHeight();
					width = sprite.getIconWidth();
					height = sprite.getIconHeight() * sprite.getFrameCount();

					BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
					for (int i = 0; i < sprite.getFrameCount(); i++) {
						System.arraycopy(sprite.getFrameTextureData(i)[0], 0, pixels, i * frameSize, frameSize);
					}
					return img;
				} catch (Exception e) {
					// eat
				}
			}
		}

		try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(toTextureFilePath(location)); InputStream stream = resource.getInputStream()) {
			return TextureUtil.readBufferedImage(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IBakedModel getItemModel(ItemStack stack) {
		return getItemModel(stack, null, null);
	}

	public static IBakedModel getItemModel(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
		if (renderItem == null) {
			renderItem = Minecraft.getMinecraft().getRenderItem();
		}

		return renderItem.getItemModelWithOverrides(stack, world, entity);
	}

	public static TextureAtlasSprite[] getBlockSprites(ItemStack stack) {
		return getBlockSprites(stack, null, null);
	}

	public static TextureAtlasSprite[] getBlockSprites(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
		TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];

		try {
			IBakedModel missingModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
			IBakedModel model;
			IBlockState state = null;
			if (stack.getItem() instanceof ItemBlock) {
				state = ItemUtils.getBlockState(stack);
				model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

				if (model == missingModel) {
					model = getItemModel(stack, world, entity);
				}
			} else {
				model = getItemModel(stack, world, entity);
			}

			if (model != missingModel) {
				List<BakedQuad> generalQuads = model.getQuads(state, null, 0L);

				for (EnumFacing f : EnumFacing.VALUES) {
					List<BakedQuad> quads = model.getQuads(state, f, 0L);
					Set<TextureAtlasSprite> foundTextures;

					foundTextures = quads.stream().map(BakedQuad::getSprite).collect(Collectors.toCollection(Sets::newIdentityHashSet));
					if (foundTextures.size() != 1) {
						foundTextures = quads.stream().filter(q -> q.getFace() == f || q.getFace() == null).map(BakedQuad::getSprite).collect(Collectors.toCollection(Sets::newIdentityHashSet));
						if (foundTextures.size() != 1) {
							foundTextures = quads.stream().filter(q -> q.getFace() == f).map(BakedQuad::getSprite).collect(Collectors.toCollection(Sets::newIdentityHashSet));
							if (foundTextures.size() != 1) {
								foundTextures = generalQuads.stream().map(BakedQuad::getSprite).collect(Collectors.toCollection(Sets::newIdentityHashSet));
								if (foundTextures.size() != 1) {
									foundTextures = generalQuads.stream().filter(q -> q.getFace() == f).map(BakedQuad::getSprite).collect(Collectors.toCollection(Sets::newIdentityHashSet));
								}
							}
						}
					}

					if (foundTextures.size() == 1) {
						sprites[f.ordinal()] = foundTextures.iterator().next();
					} else {
						sprites[f.ordinal()] = model.getParticleTexture();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sprites;
	}

	public static TextureAtlasSprite getItemSprite(ItemStack stack) {
		return getItemSprite(stack, null);
	}

	public static TextureAtlasSprite getItemSprite(ItemStack stack, @Nullable EnumFacing facing) {
		return getItemSprite(stack, null, null, facing);
	}

	public static TextureAtlasSprite getItemSprite(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity, @Nullable EnumFacing facing) {
		if (facing == null) {
			IBakedModel model = getItemModel(stack, world, entity);
			TextureAtlasSprite sprite = model.getParticleTexture();

			if ("missingno".equals(sprite.getIconName())) {
				try {
					// TODO: I probably should try to find the matching IBlockState, but it's hard.
					// TODO: Thus, let's get clever-er.
					Set<TextureAtlasSprite> foundTextures = Sets.newIdentityHashSet();
					for (EnumFacing f : EnumFacing.VALUES) {
						for (BakedQuad q : model.getQuads(null, f, 0L)) {
							foundTextures.add(q.getSprite());
						}
					}

					for (BakedQuad q : model.getQuads(null, null, 0L)) {
						foundTextures.add(q.getSprite());
					}

					// We already have missingno, so let's just remove it to make sure.
					foundTextures.remove(sprite);

					if (foundTextures.size() == 1) {
						return foundTextures.iterator().next();
					}
				} catch (Exception e) {
					// pass
				}
			}

			return sprite;
		} else {
			try {
				IBakedModel missingModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
				IBakedModel model;
				IBlockState state = null;
				if (stack.getItem() instanceof ItemBlock) {
					state = ItemUtils.getBlockState(stack);
					model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

					if (model == missingModel) {
						model = getItemModel(stack, world, entity);
					}
				} else {
					model = getItemModel(stack, world, entity);
				}

				if (model != missingModel) {
					// Step one: Try to find a texture on the facing side.
					Set<TextureAtlasSprite> foundTextures = Sets.newIdentityHashSet();

					for (BakedQuad quad : model.getQuads(state, facing, 0L)) {
						foundTextures.add(quad.getSprite());
					}

					if (foundTextures.size() == 1) {
						return foundTextures.iterator().next();
					} else if (foundTextures.isEmpty()) {
						// Step two: Try to find a general texture with the facing side.
						for (BakedQuad quad : model.getQuads(state, null, 0L)) {
							if (quad.getFace() == facing) {
								foundTextures.add(quad.getSprite());
							}
						}

						if (foundTextures.size() == 1) {
							return foundTextures.iterator().next();
						}
					}
				}
			} catch (Exception e) {
				// pass
			}

			// fallback
			return getItemSprite(stack, world, entity, null);
		}
	}

	public static int asMcIntColor(float[] data) {
		for (int i = 0; i < data.length; i++) {
			if (data[i] < 0.0f) data[i] = 0.0f;
			else if (data[i] > 1.0f) data[i] = 1.0f;
		}

		int color = (Math.round(data[0] * 255) << 16) | (Math.round(data[1] * 255) << 8) | Math.round(data[2] * 255);
		if (data.length >= 4) {
			color |= (Math.round(data[3] * 255) << 24);
		} else {
			color |= 0xFF000000;
		}
		return color;
	}

	public static int asMcIntColor(double[] data) {
		for (int i = 0; i < data.length; i++) {
			if (data[i] < 0.0) data[i] = 0.0;
			else if (data[i] > 1.0) data[i] = 1.0;
		}

		int color = ((int)Math.round(data[0] * 255) << 16) | ((int)Math.round(data[1] * 255) << 8) | (int)Math.round(data[2] * 255);
		if (data.length >= 4) {
			color |= ((int)Math.round(data[3] * 255) << 24);
		} else {
			color |= 0xFF000000;
		}
		return color;
	}

	public static void glColor(int color) {
		GlStateManager.color((((color >> 16) & 0xFF) / 255.0f), (((color >> 8) & 0xFF) / 255.0f), ((color & 0xFF) / 255.0f), (((color >> 24) & 0xFF) / 255.0f));
	}

	public static void glColor(int color, float alpha) {
		GlStateManager.color((((color >> 16) & 0xFF) / 255.0f), (((color >> 8) & 0xFF) / 255.0f), ((color & 0xFF) / 255.0f), (((color >> 24) & 0xFF) / 255.0f) * alpha);
	}

	public static float[] calculateUV(Vector3f from, Vector3f to, EnumFacing facing1) {
		if (to.y > 16) {
			from = new Vector3f(from);
			to = new Vector3f(to);
			while (to.y > 16) {
				from.y -= 16;
				to.y -= 16;
			}
		}

		EnumFacing facing = facing1;
		if (facing == null) {
			if (from.y == to.y) {
				facing = EnumFacing.UP;
			} else if (from.x == to.x) {
				facing = EnumFacing.EAST;
			} else if (from.z == to.z) {
				facing = EnumFacing.SOUTH;
			} else {
				return null; // !?
			}
		}

		switch (facing) {
			case DOWN:
				return new float[] {from.x, 16.0F - to.z, to.x, 16.0F - from.z};
			case UP:
				return new float[] {from.x, from.z, to.x, to.z};
			case NORTH:
				return new float[] {16.0F - to.x, 16.0F - to.y, 16.0F - from.x, 16.0F - from.y};
			case SOUTH:
				return new float[] {from.x, 16.0F - to.y, to.x, 16.0F - from.y};
			case WEST:
				return new float[] {from.z, 16.0F - to.y, to.z, 16.0F - from.y};
			case EAST:
				return new float[] {16.0F - to.z, 16.0F - to.y, 16.0F - from.z, 16.0F - from.y};
		}

		return null;
	}

	public static IModel getModel(ResourceLocation location) {
		try {
			return ModelLoaderRegistry.getModel(location);
		} catch (Exception e) {
			ModCharset.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
			e.printStackTrace();
			return null;
		}
	}

	public static IModel getModelWithTextures(ResourceLocation location, TextureMap map) {
		IModel model = getModel(location);
		if (model != null) {
			for (ResourceLocation tlocation : getAllTextures(model)) {
				map.registerSprite(tlocation);
			}
		}
		return model;
	}

	public static Collection<ResourceLocation> getAllTextures(IModel model) {
		Collection<ResourceLocation> textures = new ArrayList<>(model.getTextures());

		LinkedList<ResourceLocation> locs = new LinkedList<>();
		locs.addAll(model.getDependencies());
		while (!locs.isEmpty()) {
			IModel m = RenderUtils.getModel(locs.remove());
			if (m != null) {
				textures.addAll(m.getTextures());
				locs.addAll(m.getDependencies());
			}
		}
		return textures;
	}

	private static int getSelectionMask(int y, int x, int z) {
		return 1 << (y * 4 + x * 2 + z);
	}

	private static void drawLine(BufferBuilder worldrenderer, Tessellator tessellator, double x1, double y1, double z1, double x2, double y2, double z2) {
		worldrenderer.pos(x1, y1, z1).endVertex();
		worldrenderer.pos(x2, y2, z2).endVertex();
	}

	private static final int[] selectionMask;

	static {
		selectionMask = new int[6];
		selectionMask[0] = 0x00F;
		selectionMask[1] = 0xF00;

		int lineMask = 0;
		lineMask |= getSelectionMask(1, 0, 0);
		lineMask |= getSelectionMask(1, 1, 0);
		lineMask |= getSelectionMask(0, 0, 0);
		lineMask |= getSelectionMask(2, 0, 0);
		selectionMask[2] = lineMask;

		lineMask = 0;
		lineMask |= getSelectionMask(1, 0, 1);
		lineMask |= getSelectionMask(1, 1, 1);
		lineMask |= getSelectionMask(0, 0, 1);
		lineMask |= getSelectionMask(2, 0, 1);
		selectionMask[3] = lineMask;

		lineMask = 0;
		lineMask |= getSelectionMask(1, 0, 0);
		lineMask |= getSelectionMask(1, 0, 1);
		lineMask |= getSelectionMask(0, 1, 0);
		lineMask |= getSelectionMask(2, 1, 0);
		selectionMask[4] = lineMask;

		lineMask = 0;
		lineMask |= getSelectionMask(1, 1, 0);
		lineMask |= getSelectionMask(1, 1, 1);
		lineMask |= getSelectionMask(0, 1, 1);
		lineMask |= getSelectionMask(2, 1, 1);
		selectionMask[5] = lineMask;
	}

	public static int getSelectionMask(EnumFacing face) {
		return selectionMask[face.ordinal()];
	}

	public static void drawSelectionBoundingBox(AxisAlignedBB box, int lineMask) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

		Vec3d cameraPos = EntityUtils.interpolate(player, partialTicks);
		AxisAlignedBB boundingBox = box.grow(0.002).offset(cameraPos.scale(-1));
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		if ((lineMask & /* getSelectionMask(0, 0, 0) */ 0x001) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(0, 0, 1) */ 0x002) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(0, 1, 0) */ 0x004) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(0, 1, 1) */ 0x008) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(1, 0, 0) */ 0x010) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(1, 0, 1) */ 0x020) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(1, 1, 0) */ 0x040) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(1, 1, 1) */ 0x080) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(2, 0, 0) */ 0x100) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(2, 0, 1) */ 0x200) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(2, 1, 0) */ 0x400) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(2, 1, 1) */ 0x800) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static int multiplyColor(int src, int dst) {
		int out = 0;
		for (int i = 0; i < 32; i += 8) {
			out |= ((((src >> i) & 0xFF) * ((dst >> i) & 0xFF) / 0xFF) & 0xFF) << i;
		}
		return out;
	}
}
