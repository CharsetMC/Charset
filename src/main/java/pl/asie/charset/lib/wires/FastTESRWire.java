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

package pl.asie.charset.lib.wires;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.FastTESR;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.module.power.mechanical.TileAxle;
import pl.asie.charset.module.power.mechanical.render.TileAxleRenderer;

import java.util.concurrent.ExecutionException;

public class FastTESRWire extends FastTESR<TileWire> {
	protected static BlockModelRenderer renderer;

	@Override
	public void renderTileEntityFast(TileWire te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		BlockPos pos = te.getPos();
		Wire wire = te.wire;
		if (wire == null) {
			return;
		}

		IBakedModel m = CharsetLibWires.rendererWire.bakeWire(wire, true, false, null);
		if (m != null) {
			buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
			renderer.renderModel(getWorld(), m, CharsetLibWires.blockWire.getDefaultState(), pos, buffer, false);
			buffer.setTranslation(0, 0, 0);
		}
	}
}
