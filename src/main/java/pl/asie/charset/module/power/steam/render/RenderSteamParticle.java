/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.power.steam.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import pl.asie.charset.lib.utils.EntityUtils;
import pl.asie.charset.lib.utils.MathUtils;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.module.power.steam.CharsetPowerSteam;
import pl.asie.charset.module.power.steam.SteamChunkContainer;
import pl.asie.charset.module.power.steam.SteamParticle;
import pl.asie.charset.module.power.steam.api.IMirror;

import java.util.Optional;

public class RenderSteamParticle {
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		Minecraft.getMinecraft().mcProfiler.startSection("charset_steam");
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		float rotationX = ActiveRenderInfo.getRotationX();
		float rotationYZ = ActiveRenderInfo.getRotationZ();
		float rotationZ = ActiveRenderInfo.getRotationXZ();
		float rotationXZ = ActiveRenderInfo.getRotationXY();
		float rotationXY = ActiveRenderInfo.getRotationYZ();

		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("charset:blocks/steam");

		float[] spritePositions = {
				sprite.getMaxU(), sprite.getMaxV(),
				sprite.getMaxU(), sprite.getMinV(),
				sprite.getMinU(), sprite.getMinV(),
				sprite.getMinU(), sprite.getMaxV()
		};

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.enableAlpha();
		//RenderHelper.enableStandardItemLighting();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
		Vec3d cameraPos = EntityUtils.interpolate(cameraEntity, event.getPartialTicks());

		ICamera camera = new Frustum();
		camera.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);

		worldrenderer.setTranslation(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		for (SteamChunkContainer chunkContainer : world.getCapability(CharsetPowerSteam.steamWorldCap, null).getAllContainers()) {
			for (SteamParticle particle : chunkContainer.getParticles()) {
				if (particle.isInvalid()) {
					continue;
				}

				float size = 0.1f * (float) Math.log10(particle.getValue());
				float alpha = 0.25f;

				Vec3d pos = particle.getPosition(Minecraft.getMinecraft().getRenderPartialTicks());
				AxisAlignedBB box = new AxisAlignedBB(pos.addVector(-size, -size, -size), pos.addVector(size, size, size));

				if (!camera.isBoundingBoxInFrustum(box)) {
					continue;
				}

				int l = world.getCombinedLight(new BlockPos(pos), 0);
				int j = (l >> 16) & 0xFFFF;
				int k = l & 0xFFFF;
				Vec3d[] positions = new Vec3d[] {
						new Vec3d((double)(-rotationX * size - rotationXY * size), (double)(-rotationZ * size), (double)(-rotationYZ * size - rotationXZ * size)),
						new Vec3d((double)(-rotationX * size + rotationXY * size), (double)(rotationZ * size), (double)(-rotationYZ * size + rotationXZ * size)),
						new Vec3d((double)(rotationX * size + rotationXY * size), (double)(rotationZ * size), (double)(rotationYZ * size + rotationXZ * size)),
						new Vec3d((double)(rotationX * size - rotationXY * size), (double)(-rotationZ * size), (double)(rotationYZ * size - rotationXZ * size))
				};

				for (int i = 0; i < 4; i++) {
					worldrenderer.pos(
							positions[i].x + pos.x,
							positions[i].y + pos.y,
							positions[i].z + pos.z
					).tex(spritePositions[i*2], spritePositions[i*2+1]).color(1, 1, 1, alpha)
							.lightmap(j, k).endVertex();
				}
			}
		}

		if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
			tessellator.getBuffer().sortVertexData((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
		}
		tessellator.draw();

		worldrenderer.setTranslation(0,0,0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.disableBlend();

		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
