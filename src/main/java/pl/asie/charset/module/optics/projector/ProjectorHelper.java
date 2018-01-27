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

package pl.asie.charset.module.optics.projector;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.Orientation;

public class ProjectorHelper {
	public static final float OFFSET = 1/256f;
	public static final ProjectorHelper INSTANCE = new ProjectorHelper();

	private ProjectorHelper() {

	}

	public Orientation getOrientation(IProjectorSurface surface) {
		int offset = (surface.getScreenFacing().ordinal() ^ 1) * 4;
		for (int i = 0; i < 4; i++) {
			Orientation o = Orientation.values()[offset + i];
			if ((o.getRotation()&3) == (surface.getRotation()&3)) {
				return o;
			}
		}

		// ??
		ModCharset.logger.error("Could not find Orientation for [" + surface.getScreenFacing().getOpposite() + ", " + surface.getRotation() + "]!");
		return Orientation.FACE_WEST_POINT_UP;
	}

	public void renderTexture(IProjectorSurface surface, int uStart, int uEnd, int vStart, int vEnd) {
		double[] data = {
				surface.getCornerStart().y,
				surface.getCornerEnd().y,
				surface.getCornerStart().z,
				surface.getCornerEnd().z,
				surface.getCornerStart().x,
				surface.getCornerEnd().x
		};

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		float[] uvValues = surface.createUvArray(uStart, uEnd, vStart, vEnd);

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		EnumFaceDirection efd = EnumFaceDirection.getFacing(surface.getScreenFacing());
		for (int i = 0; i < 4; i++) {
			EnumFaceDirection.VertexInformation vi = efd.getVertexInformation(i);
			worldrenderer
					.pos(data[vi.xIndex], data[vi.yIndex], data[vi.zIndex])
					.tex(uvValues[i * 2], uvValues[i * 2 + 1])
					.endVertex();
		}

		tessellator.draw();
	}
}
