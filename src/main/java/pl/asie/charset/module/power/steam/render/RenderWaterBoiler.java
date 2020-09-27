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

package pl.asie.charset.module.power.steam.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.steam.CharsetPowerSteam;
import pl.asie.charset.module.power.steam.TileWaterBoiler;

public class RenderWaterBoiler extends FastTESR<TileWaterBoiler> {
	private static final int ACCURACY = 16;
	private static final float MAX_HEAT = 0.4f;

	public static final RenderWaterBoiler INSTANCE = new RenderWaterBoiler();
	protected static BlockModelRenderer renderer;
	private IModel heatOverlayModel;
	private IBakedModel[] heatOverlayBaked;

	private RenderWaterBoiler() {

	}

	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		heatOverlayModel = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/heat_overlay"), event.getMap());
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		IBakedModel heatOverlay = heatOverlayModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
		heatOverlayBaked = new IBakedModel[ACCURACY];
		for (int i = 0; i < ACCURACY; i++) {
			float a = (i + 1) * MAX_HEAT / ACCURACY;
			int color = (((int) (a * 255)) << 24) | 0xFFFFFF;
			try {
				heatOverlayBaked[i] = ModelTransformer.transform(heatOverlay, CharsetPowerSteam.blockWaterBoiler.getDefaultState(), 0L, ModelTransformer.IVertexTransformer.tint(color));
			} catch (ModelTransformer.TransformationFailedException e) {
				ModCharset.logger.warn("Failed to transform heat overlay model!", e);
				heatOverlayBaked[i] = heatOverlay;
			}
		}
	}

	@Override
	public void renderTileEntityFast(TileWaterBoiler te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		if (te.getGivenHeat() > 0 && heatOverlayBaked != null) {
			if (renderer == null) {
				renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
			}

			BlockPos pos = te.getPos();
			buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

			int v = (int) (te.getGivenHeatClient() * (ACCURACY - 1) / MAX_HEAT);
			renderer.renderModel(getWorld(), heatOverlayBaked[v], CharsetPowerSteam.blockWaterBoiler.getDefaultState(), pos, buffer, false);
			buffer.setTranslation(0, 0, 0);
		}
	}
}
