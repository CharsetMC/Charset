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

package pl.asie.charset.module.optics.laser;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.command.CommandCharset;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleMultiLayerBakedModel;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.module.optics.laser.blocks.LaserTintHandler;
import pl.asie.charset.module.optics.laser.system.LaserRenderer;
import pl.asie.charset.module.optics.laser.system.SubCommandDebugLasersClient;

public class ProxyClient extends ProxyCommon {
	private static IModel prismModel;

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new LaserRenderer());
		CommandCharset.register(new SubCommandDebugLasersClient());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorBlock(ColorHandlerEvent.Block event) {
		event.getBlockColors().registerBlockColorHandler(LaserTintHandler.INSTANCE, CharsetLaser.blockCrystal, CharsetLaser.blockReflector, CharsetLaser.blockJar);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(LaserTintHandler.INSTANCE, CharsetLaser.itemCrystal, CharsetLaser.itemReflector, CharsetLaser.itemJar);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void addCustomModels(TextureStitchEvent.Pre event) {
		prismModel = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/laser_prism"), event.getMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void bakeModels(ModelBakeEvent event) {
		if (prismModel != null) {
			for (Orientation o : Orientation.values()) {
				ModelResourceLocation location = new ModelResourceLocation("charset:laser_prism", "orientation=" + o.name().toLowerCase());
				IBakedModel model = prismModel.bake(o.toTransformation(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
				event.getModelRegistry().putObject(location, model);
			}
		}

		// Patch jars to glow
		for (EnumFacing facing : EnumFacing.VALUES) {
			for (LaserColor color : LaserColor.VALUES) {
				if (color == LaserColor.NONE) continue;

				IBlockState state = CharsetLaser.blockJar.getDefaultState().withProperty(CharsetLaser.LASER_COLOR, color).withProperty(Properties.FACING, facing);
				ModelResourceLocation location = new ModelResourceLocation("charset:light_jar", "color=" + color.getName() + ",facing=" + facing.getName());
				IBakedModel model = event.getModelRegistry().getObject(location);
				VertexFormat format = new VertexFormat(DefaultVertexFormats.ITEM);
				format.addElement(DefaultVertexFormats.TEX_2S);

				if (model != null) {
					SimpleMultiLayerBakedModel result = new SimpleMultiLayerBakedModel(model);

					BlockRenderLayer layerPre = MinecraftForgeClient.getRenderLayer();
					for (BlockRenderLayer layer : new BlockRenderLayer[] { BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT }) {
						ForgeHooksClient.setRenderLayer(layer);
						for (int i = 0; i <= 6; i++) {
							EnumFacing facingIn = (i < 6) ? EnumFacing.getFront(i) : null;
							for (BakedQuad quadIn : model.getQuads(state, facingIn, 0)) {
								result.addQuad(layer, facingIn, ModelTransformer.transform(quadIn, (quad, element, data) -> {
									if (quad.getTintIndex() == 0 && element == DefaultVertexFormats.TEX_2S) {
										return new float[] { 15f * 0x20 / 0xFFFF, 0, 0, 0 };
									}
									return data;
								}, (bakedQuad -> {
									if (bakedQuad.getTintIndex() == 0) {
										return format;
									} else {
										return bakedQuad.getFormat();
									}
								})));
							}
						}
					}

					ForgeHooksClient.setRenderLayer(layerPre);
					event.getModelRegistry().putObject(location, result);
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		for (int i = 0; i <= 7; i++) {
			RegistryUtils.registerModel(CharsetLaser.itemJar, i, "charset:light_jar");
			if (i > 0)
				RegistryUtils.registerModel(CharsetLaser.itemCrystal, i, "charset:laser_crystal");
		}

		for (int i = 0; i <= 16; i++) {
			if ((i & 7) != 0) {
				if ((i & 8) != 0) {
					RegistryUtils.registerModel(CharsetLaser.itemReflector, i, "charset:laser_reflector#inventory_splitter");
				} else {
					RegistryUtils.registerModel(CharsetLaser.itemReflector, i, "charset:laser_reflector#inventory");
				}
			}
		}

		RegistryUtils.registerModel(CharsetLaser.itemPrism, 0, "charset:laser_prism#orientation=face_north_point_up");
	}
}
