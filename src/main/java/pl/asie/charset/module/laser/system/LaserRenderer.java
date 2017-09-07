/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.laser.system;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import pl.asie.charset.module.laser.CharsetLaser;

import java.util.Collection;

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

		Minecraft.getMinecraft().mcProfiler.startSection("lasers");

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.enableAlpha();
		GlStateManager.disableTexture2D();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		EntityPlayer player = Minecraft.getMinecraft().player;
		double cameraX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * event.getPartialTicks());
		double cameraY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * event.getPartialTicks());
		double cameraZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * event.getPartialTicks());

		ICamera camera = new Frustum();
		camera.setPosition(cameraX, cameraY, cameraZ);

		worldrenderer.setTranslation(-cameraX, -cameraY, -cameraZ);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		for (LaserBeam beam : beams) {
			Vec3d startVec = new Vec3d(beam.getStart()).addVector(8/16.0, 8/16.0, 8/16.0);
			Vec3d endVec = beam.calculateEndpoint();

			if (beam.getStart().getX() == beam.getEnd().getX()) {
				startVec = startVec.addVector(-THICKNESS/16.0, 0, 0);
				endVec = endVec.addVector(THICKNESS/16.0, 0, 0);
			}

			if (beam.getStart().getY() == beam.getEnd().getY()) {
				startVec = startVec.addVector(0, -THICKNESS/16.0, 0);
				endVec = endVec.addVector(0, THICKNESS/16.0, 0);
			}

			if (beam.getStart().getZ() == beam.getEnd().getZ()) {
				startVec = startVec.addVector(0, 0, -THICKNESS/16.0);
				endVec = endVec.addVector(0, 0, THICKNESS/16.0);
			}

			if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(startVec, endVec))) {
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

		tessellator.draw();

		worldrenderer.setTranslation(0,0,0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
