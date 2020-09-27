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

package pl.asie.charset.lib.render.model;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nullable;
import java.util.*;

public class SimpleMultiLayerBakedModel extends SimpleBakedModel {
	private final Table<BlockRenderLayer, EnumFacing, List<BakedQuad>> quads = Tables.newCustomTable(
			new EnumMap<>(BlockRenderLayer.class), () -> new EnumMap<>(EnumFacing.class)
	);
	private final Map<BlockRenderLayer, List<BakedQuad>> quadsUnsided = new EnumMap<>(BlockRenderLayer.class);

	public SimpleMultiLayerBakedModel() {
		super();
	}

	public SimpleMultiLayerBakedModel(IBakedModel parent) {
		super(parent);
	}

	public void addQuad(BlockRenderLayer layer, EnumFacing side, BakedQuad quad) {
		if (side == null) {
			quadsUnsided.computeIfAbsent(layer, (a) -> new ArrayList<>()).add(quad);
		} else {
			List<BakedQuad> list = quads.get(layer, side);
			if (list == null) {
				quads.put(layer, side, (list = new ArrayList<>()));
			}
			list.add(quad);
		}

		addQuad(side, quad);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		if (layer != null) {
			if (side != null) {
				List<BakedQuad> list = quads.get(layer, side);
				return list != null ? list : Collections.emptyList();
			} else {
				return quadsUnsided.getOrDefault(layer, Collections.emptyList());
			}
		} else {
			return super.getQuads(state, side, rand);
		}
	}
}
