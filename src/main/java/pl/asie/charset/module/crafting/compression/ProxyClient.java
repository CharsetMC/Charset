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

package pl.asie.charset.module.crafting.compression;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.vecmath.Vector3f;
import java.util.List;

public class ProxyClient extends ProxyCommon {
	private static final ModelResourceLocation bmLoc = new ModelResourceLocation("charset:compression_crafter", "normal");
	private static final ModelRotation[] ROTATIONS = new ModelRotation[] {
			ModelRotation.X180_Y0,
			ModelRotation.X0_Y0,
			ModelRotation.X90_Y0,
			ModelRotation.X90_Y180,
			ModelRotation.X90_Y270,
			ModelRotation.X90_Y90
	};

	protected static final IBakedModel[] rodModels = new IBakedModel[6];

	public void init() {
		super.init();
		ClientRegistry.bindTileEntitySpecialRenderer(TileCompressionCrafter.class, new TileCompressionCrafterRenderer());
		MinecraftForge.EVENT_BUS.register(CompressionShapeRenderer.INSTANCE);
	}

	@Override
	public void markShapeRender(TileCompressionCrafter sender, CompressionShape shape) {
		if (shape.world.isRemote) {
			CompressionShapeRenderer.INSTANCE.addShape(shape);
		} else {
			super.markShapeRender(sender, shape);
		}
	}

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		ModelLoader.setCustomStateMapper(CharsetCraftingCompression.blockCompressionCrafter, blockIn -> {
			ImmutableMap.Builder<IBlockState, ModelResourceLocation> builder = new ImmutableMap.Builder<>();
			for (IBlockState state : blockIn.getBlockState().getValidStates()) {
				builder.put(state, bmLoc);
			}
			return builder.build();
		});
	}

	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_bottom"));
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_top"));
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_side"));
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_inner"));
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		IBakedModel origModel = event.getModelRegistry().getObject(bmLoc);
		ModelCompressionCrafter result = new ModelCompressionCrafter(origModel);
		event.getModelRegistry().putObject(bmLoc, result);

		ProgressManager.ProgressBar bar = ProgressManager.push("Compression Crafter", 6 + (4*12));

		IModel modelRod = RenderUtils.getModel(new ResourceLocation("charset:block/compression_crafter_rod"));
		for (EnumFacing side : EnumFacing.VALUES) {
			bar.step("rod " + side.name());
			rodModels[side.ordinal()] = modelRod.bake(
					ROTATIONS[side.ordinal()],
					DefaultVertexFormats.ITEM,
					ModelLoader.defaultTextureGetter()
			);
		}

		IModel model = RenderUtils.getModel(new ResourceLocation("charset:block/compression_crafter_block"));
		IBlockState defState = CharsetCraftingCompression.blockCompressionCrafter.getDefaultState();

		for (int i = 0; i < 4; i++) {
			IModel retexModel = model.retexture(
					ImmutableMap.of(
							"top", "charset:blocks/compact/compact_inner#" + i,
							"bottom", "charset:blocks/compact/compact_bottom#" + i,
							"side_x", "charset:blocks/compact/compact_side#" + i,
							"side_z", "charset:blocks/compact/compact_side#" + i
					)
			);

			for (int k = 0; k < 12; k++) {
				bar.step("crafter " + (i * 4 + k + 1) + "/48");
				if (i == 0 && k >= 6) {
					for (EnumFacing side : EnumFacing.VALUES) {
						result.quads[k][side.ordinal()][i] = result.quads[k - 6][side.ordinal()][i];
					}
					continue;
				}

				EnumFacing facing = EnumFacing.byIndex(k % 6);
				IModelState modelState = ROTATIONS[k % 6];
				if (k >= 6) {
					modelState = new ModelStateComposition(
							modelState,
							ModelRotation.X0_Y90
					);
				}

				IBakedModel bakedModel = retexModel.bake(
						modelState,
						DefaultVertexFormats.ITEM,
						ModelLoader.defaultTextureGetter()
				);

				for (EnumFacing side : EnumFacing.VALUES) {
					List<BakedQuad> list = bakedModel.getQuads(defState, side, 0);
					int j = i;
					if ((facing == EnumFacing.DOWN && (side == EnumFacing.SOUTH || side == EnumFacing.WEST))
							|| (facing == EnumFacing.UP && (side == EnumFacing.NORTH || side == EnumFacing.EAST))
							|| ((facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) &&
							(side.getAxis() == EnumFacing.Axis.Y || (side.getAxis() == facing.getAxis() && k < 8)))
							|| (facing == EnumFacing.DOWN && side == EnumFacing.UP && k >= 6)
							|| (facing.getAxis() != EnumFacing.Axis.Y && k >= 8 && side == facing.rotateY())) {
						if (j == 1) j = 2;
						else if (j == 2) j = 1;
					}

					result.quads[k][side.ordinal()][j] = list.get(0);
				}
			}
		}

		ProgressManager.pop(bar);
	}
}
