/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.optics.laser.system;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import pl.asie.charset.lib.utils.EntityUtils;
import pl.asie.charset.lib.utils.MathUtils;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.module.optics.laser.CharsetLaser;

import java.util.*;

public class LaserRenderer {
	private static final float THICKNESS = 0.5f;
	private int renderedLasers, totalLasers;

	@SubscribeEvent
	public void onGameOverlayDebugRender(RenderGameOverlayEvent.Text event) {
		if (!Minecraft.getMinecraft().gameSettings.showDebugInfo)
			return;

		if (!SubCommandDebugLasersClient.enabled)
			return;

		event.getLeft().add("");
		event.getLeft().add("[CharsetLaser] L: " + renderedLasers + "/" + totalLasers);
	}

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		Collection<LaserBeam> beams = CharsetLaser.laserStorage.getLaserBeams(world);

		renderedLasers = 0;
		totalLasers = beams.size();

		if (totalLasers == 0) return;

		Minecraft.getMinecraft().profiler.startSection("lasers");

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
		Vec3d cameraPos = EntityUtils.interpolate(cameraEntity, event.getPartialTicks());

		ICamera camera = new Frustum();
		camera.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);

		int maxDist = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 + 1;

		Collection<LaserBeam> beamsRender = beams;

		worldrenderer.setTranslation(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		for (LaserBeam beam : beamsRender) {
			beam.vcstart = beam.calculateStartpoint();
			beam.vcend = beam.calculateEndpoint();
			beam.vcdist = MathUtils.linePointDistance(beam.vcstart, beam.vcend, cameraPos);

			if (beam.vcdist > maxDist) {
				continue;
			}

			Vec3d startVec = beam.vcstart;
			Vec3d endVec = beam.vcend;
			float t = THICKNESS;
			if (beam.getColor().red) t += 0.004f;
			if (beam.getColor().green) t += 0.002f;
			if (beam.getColor().blue) t += 0.001f;

			if (beam.getStart().getX() == beam.getEnd().getX()) {
				startVec = startVec.add(-t / 16.0, 0, 0);
				endVec = endVec.add(t / 16.0, 0, 0);
			}

			if (beam.getStart().getY() == beam.getEnd().getY()) {
				startVec = startVec.add(0, -t / 16.0, 0);
				endVec = endVec.add(0, t / 16.0, 0);
			}

			if (beam.getStart().getZ() == beam.getEnd().getZ()) {
				startVec = startVec.add(0, 0, -t / 16.0);
				endVec = endVec.add(0, 0, t / 16.0);
			}

			if (!camera.isBoundingBoxInFrustum(SpaceUtils.from(startVec, endVec))) {
				continue;
			}

			renderedLasers++;

			int color = CharsetLaser.LASER_COLORS[beam.getColor().ordinal()];
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			int a = (color >> 24) & 0xFF;

			double[] data = new double[] {
				startVec.y,
				endVec.y,
				startVec.z,
				endVec.z,
				startVec.x,
				endVec.x
			};

			for (EnumFacing facing : EnumFacing.VALUES) {
				for (int i = 0; i < 4; i++) {
					EnumFaceDirection.VertexInformation vi = EnumFaceDirection.getFacing(facing).getVertexInformation(i);
					worldrenderer.pos(data[vi.xIndex], data[vi.yIndex], data[vi.zIndex]).color(r, g, b, a).endVertex();
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
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		Minecraft.getMinecraft().profiler.endSection();
	}
}
