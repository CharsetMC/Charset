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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataBands;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataDots;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelSignalMeter extends WrappedBakedModel {
	public static TextureAtlasSprite WHITE;
	private final ISignalMeterData data;

	private static final ItemOverrideList OVERRIDE_LIST = new ItemOverrideList(Collections.emptyList()) {
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			if (entity != null && entity.hasCapability(CharsetToolsEngineering.meterTrackerCap, null)
				&& (stack == entity.getHeldItemMainhand() || stack == entity.getHeldItemOffhand())) {
				return new ModelSignalMeter(((ModelSignalMeter) originalModel).getParent(), entity.getCapability(CharsetToolsEngineering.meterTrackerCap, null).getClientData());
			}

			return originalModel;
		}
	};

	public ModelSignalMeter(IBakedModel parent) {
		this(parent, null);
	}

	public ModelSignalMeter(IBakedModel parent, ISignalMeterData data) {
		super(parent);
		this.data = data;

		addDefaultItemTransforms();
		addFirstPersonTransformation(getTransformation(-1f, 3.2f, 1.13f, 0, -7f, 0, 0.68f));
	}

	private int fixColor(int v) {
		return (v & 0xFF00FF00) | ((v & 0xFF) << 16) | ((v >> 16) & 0xFF);
	}

	public float getDotSize(ISignalMeterDataDots dots) {
		return Math.min(8f / dots.getDotCount(), 1f);
	}

	public void renderDots(List<BakedQuad> quads, ISignalMeterDataDots dots) {
		float z = 8.502f;
		float dotSize = getDotSize(dots);
		float dotStart = 10.5f - (dotSize / 2);

		if (dots instanceof ISignalMeterDataBands) {
			dotStart += 2f;
		}

		float bx = 4f + ((8f - (dots.getDotCount() * dotSize)) / 2f);

		for (int i = 0; i < dots.getDotCount(); i++, bx += dotSize) {
			int col = dots.getDotColor(i);
			if ((col & 0xFF000000) != 0) {
				quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(
						new Vector3f(bx, dotStart, z),
						new Vector3f(bx + dotSize, dotStart + dotSize, z),
						fixColor(col | 0xFF000000),
						WHITE, EnumFacing.SOUTH, TRSRTransformation.identity(), false
				));
			}
		}
	}

	public void renderBands(List<BakedQuad> quads, ISignalMeterDataBands bands) {
		float z = 8.502f;
		float bandStart = 8f;
		float bandHeight = 5f;
		float bandWidth = Math.min(8f / bands.getBandCount(), 1f);

		if (bands instanceof ISignalMeterDataDots) {
			bandHeight -= (getDotSize((ISignalMeterDataDots) bands) + 1f);
		}

		float bx = 4f + ((8f - (bands.getBandCount() * bandWidth)) / 2f);

		float yStart = bandStart;
		float yEnd = bandStart + bandHeight;

		for (int i = 0; i < bands.getBandCount(); i++, bx += bandWidth) {
			float yMid = yEnd - ((1f - bands.getBandHeight(i)) * bandHeight);
			if (yMid != yStart) {
				int col = bands.getBandColor(i);
				if ((col & 0xFF000000) != 0) {
					quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(
							new Vector3f(bx, yStart, z),
							new Vector3f(bx + bandWidth, yMid, z),
							fixColor(col | 0xFF000000),
							WHITE, EnumFacing.SOUTH, TRSRTransformation.identity(), false
					));
				}
			}
			int bgCol = bands.getBandBackgroundColor(i);
			if ((bgCol & 0xFF000000) != 0 && yMid != yEnd) {
				quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(
						new Vector3f(bx, yMid, z),
						new Vector3f(bx + bandWidth, yEnd, z),
						fixColor(bgCol | 0xFF000000),
						WHITE, EnumFacing.SOUTH, TRSRTransformation.identity(), false
				));

			}
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		// WIP
		if (side == null && data != null) {
			List<BakedQuad> quads = new ArrayList<>();
			quads.addAll(super.getQuads(state, side, rand));

			if (data instanceof ISignalMeterDataDots) {
				renderDots(quads, (ISignalMeterDataDots) data);
			}

			if (data instanceof ISignalMeterDataBands) {
				renderBands(quads, (ISignalMeterDataBands) data);
			}

			return quads;
		} else {
			return super.getQuads(state, side, rand);
		}
	}

	@Override
	public ItemOverrideList getOverrides() {
		return OVERRIDE_LIST;
	}
}
