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

package pl.asie.simplelogic.gates.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.simplelogic.gates.logic.GateLogicBundledTransposer;
import pl.asie.simplelogic.gates.logic.IGateContainer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GateCustomRendererTransposer extends GateCustomRenderer<GateLogicBundledTransposer> {
	public static TextureAtlasSprite WHITE;
	public static IBakedModel[] rayModels;

	@Override
	public Class<GateLogicBundledTransposer> getLogicClass() {
		return GateLogicBundledTransposer.class;
	}

	@Override
	public void renderStatic(IGateContainer gate, GateLogicBundledTransposer logic, boolean isItem, Consumer<IBakedModel> modelConsumer, BiConsumer<BakedQuad, EnumFacing> quadConsumer) {
		if (rayModels == null) {
			rayModels = new IBakedModel[256];
		}

		float width = 0.125f/16f;
		if (isItem) {
			width *= 2;
		}

		// SOUTH -> NORTH
		for (int from = 0; from < 16; from++) {
			int v = logic.transpositionMap[from];
			int i = 0;
			while (v != 0) {
				if ((v & 1) != 0) {
					int pos = (from << 4) | i;
					if (rayModels[pos] == null || ModelFactory.DISABLE_CACHE) {
						SimpleBakedModel model = new SimpleBakedModel();

						int color1 = EnumDyeColor.byMetadata(from).getColorValue() | 0xDF000000;
						int color2 = EnumDyeColor.byMetadata(i).getColorValue() | 0xDF000000;

						float x1 = 2.375f + (from * 12F / 16F);
						float x2 = 2.375f + (i * 12F / 16F);
						float y1 = 11.025F;
						float y2 = 4.975F;
						float height = 3f / 16f - ((from * 16 + i) / 65536f);

						float x = x1;
						float y = y1;
						float xd = (x2 - x1) / 8f;
						float yd = (y2 - y1) / 8f;

						for (int step = 0; step < 8; step++, x += xd, y += yd) {
							int col = (step & 1) == 0 ? color1 : color2;
							float qx1 = x / 16f;
							float qy1 = y / 16f;
							float qx2 = (x + xd) / 16f;
							float qy2 = (y + yd) / 16f;

							VertexFormat fmt = isItem ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK;
							UnpackedBakedQuad.Builder quad = new UnpackedBakedQuad.Builder(fmt);
							quad.setTexture(WHITE);
							quad.setQuadOrientation(EnumFacing.UP);
							quad.setApplyDiffuseLighting(true);
							quad.setContractUVs(false);
							quad.setQuadTint(-1);

							for (int vc = 0; vc < 4; vc++) {
								for (int el = 0; el < fmt.getElementCount(); el++) {
									switch (fmt.getElement(el).getUsage()) {
										case POSITION:
											switch (vc) {
												case 0:
													quad.put(el, qx1 - width, height, qy1);
													break;
												case 1:
													quad.put(el, qx1 + width, height, qy1);
													break;
												case 2:
													quad.put(el, qx2 + width, height, qy2);
													break;
												case 3:
													quad.put(el, qx2 - width, height, qy2);
													break;
											}
											break;
										case NORMAL:
											quad.put(el, 0, 1, 0);
											break;
										case COLOR:
											quad.put(el, ((col >> 16) & 0xFF) / 255.0f, ((col >> 8) & 0xFF) / 255.0f, ((col) & 0xFF) / 255.0f, ((col >> 24) & 0xFF) / 255.0f);
											break;
										case UV:
											if (fmt.getElement(el).getIndex() == 0) {
												quad.put(el, WHITE.getMinU(), WHITE.getMinV());
											} else {
												quad.put(el);
											}
											break;
										default:
											quad.put(el);
											break;
									}
								}
							}

							model.addQuad(null, quad.build());
						}

						rayModels[pos] = model;
					}

					modelConsumer.accept(getTransformedModel(rayModels[pos], gate));
				}
				v >>= 1; i++;
			}
		}
	}
}
