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

package pl.asie.charset.module.crafting.compression;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.render.model.WrappedBakedModel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ModelCompressionCrafter extends WrappedBakedModel {
	protected final BakedQuad[][][] quads = new BakedQuad[12][6][4]; // [facing][side][ctm]

	public ModelCompressionCrafter(IBakedModel parent) {
		super(parent);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (state == null) {
			return Collections.emptyList();
		}


		int facing = state.getValue(Properties.FACING).ordinal();
		if (side == null) {
			side = EnumFacing.byIndex(facing);
		} else if (side.ordinal() == facing) {
			return Collections.emptyList();
		}

		int offY = state.getValue(BlockCompressionCrafter.OFFSET_Y);
		if (offY >= 4) {
			facing += 6;
		}

		switch (side.getAxis()) {
			case Y:
			default:
				return ImmutableList.of(quads[facing][side.ordinal()][offY & 3]);
			case X:
				return ImmutableList.of(quads[facing][side.ordinal()][state.getValue(BlockCompressionCrafter.OFFSET_X)]);
			case Z:
				return ImmutableList.of(quads[facing][side.ordinal()][state.getValue(BlockCompressionCrafter.OFFSET_Z)]);
		}
	}
}
