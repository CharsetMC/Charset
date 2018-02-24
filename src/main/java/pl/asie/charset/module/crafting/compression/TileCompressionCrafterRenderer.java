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

package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.DimensionManager;
import pl.asie.charset.lib.Properties;

public class TileCompressionCrafterRenderer extends FastTESR<TileCompressionCrafter> {
	protected static BlockModelRenderer renderer;

	@Override
	public void renderTileEntityFast(TileCompressionCrafter te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		BlockPos pos = te.getPos();
		IBlockState state = getWorld().getBlockState(pos);

		if (state.getBlock() instanceof BlockCompressionCrafter) {
			EnumFacing facing = state.getValue(Properties.FACING);
			float extension = 0f;
			if (te.shape != null) {
				extension = Math.max(0, te.shape.getRenderProgress(partialTicks));
			}

			if (te.isBackstuffedClient()) {
				extension = Math.max(0.01f * (int)(getWorld().getTotalWorldTime() & 4), extension);
			}

			double tx = x - pos.getX() + (facing.getFrontOffsetX() * extension);
			double ty = y - pos.getY() + (facing.getFrontOffsetY() * extension);
			double tz = z - pos.getZ() + (facing.getFrontOffsetZ() * extension);

			long r = MathHelper.getPositionRandom(pos);

			tx += ((r & 0x00F) - 7.5f) / 4096f;
			ty += (((r >> 4) & 0x00F) - 7.5f) / 4096f;
			tz += (((r >> 8) & 0x00F) - 7.5f) / 4096f;

			buffer.setTranslation(tx, ty, tz);
			renderer.renderModel(getWorld(), ProxyClient.rodModels[facing.ordinal()], state, pos, buffer, false);
		}
	}
}
