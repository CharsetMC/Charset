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

package pl.asie.charset.module.immersion.stacks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RenderTileEntityStacks implements IBakedModel, IStateParticleBakedModel {
	private static final int[][][] QUAD_ORDERS;
	private static final EnumFacing[][] QUAD_FACES;
	private static final int[][] QUAD_UVS;

	static {
		QUAD_ORDERS = new int[][][] {
				{
						{0, 4, 5, 1},
						{1, 5, 6, 2},
						{2, 6, 7, 3},
						{3, 7, 4, 0},
						{0, 1, 2, 3},
						{4, 7, 6, 5}
				},
				{
						{0, 1, 5, 4},
						{1, 2, 6, 5},
						{2, 3, 7, 6},
						{3, 0, 4, 7},
						{0, 3, 2, 1},
						{4, 5, 6, 7}
				}
		};

		QUAD_UVS = new int[][] {
				{0, 16},
				{16, 16},
				{16, 0},
				{0, 0}
		};

		QUAD_FACES = new EnumFacing[][] {
				{
						EnumFacing.NORTH,
						EnumFacing.EAST,
						EnumFacing.SOUTH,
						EnumFacing.WEST,
						EnumFacing.DOWN,
						EnumFacing.UP
				},
				{
						EnumFacing.WEST,
						EnumFacing.SOUTH,
						EnumFacing.EAST,
						EnumFacing.NORTH,
						EnumFacing.DOWN,
						EnumFacing.UP
				}
		};
	}

	public ModelTransformer.IVertexTransformer createTransformer(int i, ItemStack stack, long rand) {
		int c = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, 0);
		AxisAlignedBB box = StackShapes.getIngotBox(i, stack);

		float[] color = new float[]{
				MathHelper.clamp(((c >> 16) & 0xFF) / 255.0f, 0, 1),
				MathHelper.clamp(((c >> 8) & 0xFF) / 255.0f, 0, 1),
				MathHelper.clamp(((c) & 0xFF) / 255.0f, 0, 1),
				1.0f
		};

		rand += i * 17237;

		float offsetX = ((rand & 7) - 3.5f) / 256.0f;
		float offsetZ = (((rand >> 3) & 7) - 3.5f) / 256.0f;

		return (quad, element, data) -> {
			switch (element.getUsage()) {
				case POSITION:
					return new float[] {
							data[1] * 0.5f + offsetX + (float) box.minX,
							data[2] - 0.5f + 0.03125f + (float) box.minY,
							data[0] * 0.5f + offsetZ + (float) box.minZ,
							data[3]
					};
				case NORMAL:
					return new float[] {
							data[1],
							data[2],
							data[0],
							data[3]
					};
				case COLOR:
					return new float[] {
							data[0] * color[0],
							data[1] * color[1],
							data[2] * color[2],
							data[3] * color[3]
					};
				default:
					return data;
			}
		};
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side != null || !(state instanceof IExtendedBlockState)) {
			return Collections.emptyList();
		}

		TileEntityStacks stacks = ((IExtendedBlockState) state).getValue(BlockStacks.PROPERTY_TILE);
		if (stacks == null) {
			return Collections.emptyList();
		}

		List<BakedQuad> list = new ArrayList<>();

		for (int i = 0; i < 64; i++) {
			ItemStack stack = stacks.stacks[i];
			if (stack == null) {
				continue;
			}

			if (StackShapes.isIngot(stack)) {
				Vec3d[] vecs = StackShapes.INGOT_POSITIONS[i];

				ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
				ItemMaterial blockMaterial = material != null ? material.getRelated("block") : null;
				TextureAtlasSprite sprite;
				int c;

				if (blockMaterial == null) {
					sprite = RenderUtils.getItemSprite(new ItemStack(Blocks.IRON_BLOCK));
					c = ColorLookupHandler.INSTANCE.getColor(stack, RenderUtils.AveragingMode.FULL) | 0xFF000000;
				} else {
					sprite = RenderUtils.getItemSprite(blockMaterial.getStack());
					c = Minecraft.getMinecraft().getItemColors().colorMultiplier(blockMaterial.getStack(), 0);
				}

				float[] color = new float[]{
						MathHelper.clamp(((c >> 16) & 0xFF) / 255.0f, 0, 1),
						MathHelper.clamp(((c >> 8) & 0xFF) / 255.0f, 0, 1),
						MathHelper.clamp(((c) & 0xFF) / 255.0f, 0, 1),
						1.0f
				};

				int j = 0;
				int yOff = (i >> 3) & 1;

				for (int[] vecOrder : QUAD_ORDERS[yOff]) {
					UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
					EnumFacing face = QUAD_FACES[yOff][j];

					builder.setTexture(sprite);
					builder.setApplyDiffuseLighting(isAmbientOcclusion());
					builder.setContractUVs(false);
					builder.setQuadOrientation(face);

					int uv_offset = j < 4 ? 1 : 0;
					if (yOff == 1) uv_offset = 1 - uv_offset;

					for (int k = 0; k < vecOrder.length; k++) {
						Vec3d vec = vecs[vecOrder[k]];
						int[] uv = QUAD_UVS[(k + uv_offset) & 3];
						for (int e = 0; e < builder.getVertexFormat().getElementCount(); e++) {
							VertexFormatElement el = builder.getVertexFormat().getElement(e);
							switch (el.getUsage()) {
								case POSITION:
									builder.put(e, (float) vec.x / 16f, (float) vec.y / 16f, (float) vec.z / 16f, 1);
									break;
								case COLOR:
									builder.put(e, color);
									break;
								case NORMAL:
									builder.put(e, face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ(), 0);
									break;
								case UV:
									float u = sprite.getInterpolatedU(uv[0]);
									float v = sprite.getInterpolatedV(uv[1]);
									builder.put(e, u, v, 0, 1);
									break;
								default:
									builder.put(e);
							}
						}
					}

					list.add(builder.build());
					j++;
				}
			} else if (StackShapes.isGearPlate(stack)) {
				// renderer the second
				IBakedModel model = RenderUtils.getItemModel(stack, stacks.getWorld(), Minecraft.getMinecraft().player);
				ModelTransformer.IVertexTransformer transformer = createTransformer(i, stack, rand);
				for (BakedQuad quad : model.getQuads(state, null, 0)) {
					list.add(ModelTransformer.transform(quad, transformer));
				}
				for (EnumFacing facing : EnumFacing.VALUES) {
					for (BakedQuad quad : model.getQuads(state, facing, 0)) {
						list.add(ModelTransformer.transform(quad, transformer));
					}
				}
			}
		}

		return list;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return ModelLoader.defaultTextureGetter().apply(TextureMap.LOCATION_MISSING_TEXTURE);
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IBlockState state, @Nullable EnumFacing facing) {
		TextureAtlasSprite sprite = null;

		TileEntityStacks stacks = ((IExtendedBlockState) state).getValue(BlockStacks.PROPERTY_TILE);
		if (stacks != null) {
			for (int i = 63; i >= 0; i--) {
				ItemStack stack = stacks.stacks[i];
				if (stack != null && !stack.isEmpty()) {
					sprite = RenderUtils.getItemSprite(stack);
					break;
				}
			}
		}

		return sprite != null ? sprite : getParticleTexture();
	}
}
