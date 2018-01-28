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

package pl.asie.charset.module.optics.projector;

import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.lib.utils.*;
import pl.asie.charset.lib.utils.Orientation;

public class ProjectorRenderer {
	public static class Surface implements IProjectorSurface {
		public World world;
		public Vec3d cornerStart, cornerEnd;
		public Orientation orientation;
		public float width, height;
		public float r, g, b, a;

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public Vec3d getCornerStart() {
			return cornerStart;
		}

		@Override
		public Vec3d getCornerEnd() {
			return cornerEnd;
		}

		@Override
		public EnumFacing getScreenFacing() {
			return orientation.facing.getOpposite();
		}

		@Override
		public int getRotation() {
			return orientation.getRotation();
		}

		@Override
		public float getWidth() {
			return width;
		}

		@Override
		public float getHeight() {
			return height;
		}

		@Override
		public float[] createUvArray(int uStart, int uEnd, int vStart, int vEnd) {
			float[] uvValues = {
					uStart/256f, vStart/256f,
					uStart/256f, vEnd/256f,
					uEnd/256f, vEnd/256f,
					uEnd/256f, vStart/256f
			};

			for (int j = 0; j < getRotation(); j++) {
				float t = uvValues[0];
				uvValues[0] = uvValues[2];
				uvValues[2] = uvValues[4];
				uvValues[4] = uvValues[6];
				uvValues[6] = t;

				t = uvValues[1];
				uvValues[1] = uvValues[3];
				uvValues[3] = uvValues[5];
				uvValues[5] = uvValues[7];
				uvValues[7] = t;
			}

			return uvValues;
		}

		@Override
		public void restoreGLColor() {
			GlStateManager.color(r, g, b, a);
		}
	}

	public Surface getSurface(World world, BlockPos pos, Orientation orientation, float sizeFactor, float aspectRatio) {
		Surface surface = new Surface();
		EnumFacing projectorDirection = orientation.facing;
		int maxDistance = 16;

		float sizeFactorW = 1f;
		float sizeFactorH = 1f / aspectRatio;
		float sfDiv = (float) Math.sqrt(sizeFactorW * sizeFactorW + sizeFactorH * sizeFactorH);
		sizeFactorW *= sizeFactor / sfDiv;
		sizeFactorH *= sizeFactor / sfDiv;

		Vec3d bottomRightOffset = new Vec3d(
				projectorDirection.getAxis() == EnumFacing.Axis.X ? 0 : 1,
				projectorDirection.getAxis() == EnumFacing.Axis.Y ? 0 : 1,
				projectorDirection.getAxis() == EnumFacing.Axis.Z ? 0 : 1
		);

		for (int i = 1; i <= maxDistance; i++) {
			BlockPos centerPos = pos.offset(projectorDirection, i);
			Vec3d center = new Vec3d(centerPos).addVector(
					projectorDirection.getFrontOffsetX() * 0.5f + 0.5f,
					projectorDirection.getFrontOffsetY() * 0.5f + 0.5f,
					projectorDirection.getFrontOffsetZ() * 0.5f + 0.5f
			);
			Vec3d topLeft = center;
			Vec3d bottomRight = center;

			// TODO: This sucks. All of it.
			for (EnumFacing.Axis axis : EnumFacing.Axis.values()) {
				if (axis != projectorDirection.getAxis()) {
					Vec3d axisVec = new Vec3d(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis).getDirectionVec())
							.scale((axis == orientation.top.getAxis() ? sizeFactorH : sizeFactorW) * i);
					topLeft = topLeft.subtract(axisVec);
					bottomRight = bottomRight.add(axisVec);
				}
			}

			BlockPos topLeftPos = new BlockPos(Math.floor(topLeft.x), Math.floor(topLeft.y), Math.floor(topLeft.z));
			BlockPos bottomRightPos = new BlockPos(
					Math.ceil(bottomRight.x - bottomRightOffset.x),
					Math.ceil(bottomRight.y - bottomRightOffset.y),
					Math.ceil(bottomRight.z - bottomRightOffset.z));
			int solidCount = 0;
			int totalCount = 0;
			boolean isSurface = true;
			boolean hasBlocks = false;

			for (BlockPos checkPos : BlockPos.getAllInBox(topLeftPos, bottomRightPos)) {
				totalCount++;
				IBlockState state = world.getBlockState(checkPos);
				if (!state.getMaterial().isOpaque()) {
					isSurface = false;
				} else {
					hasBlocks = true;
					if (state.isSideSolid(world, pos, projectorDirection.getOpposite())) {
						solidCount++;
					}
				}
			}

			if (!isSurface && solidCount > 0) {
				return null;
			} else if (isSurface && solidCount == totalCount) {
				float v = projectorDirection.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1f : 0f;
				v += ProjectorHelper.OFFSET + (pos.hashCode() & 31) / 16384f;
				surface.world = world;
				surface.cornerStart = topLeft.subtract(
						projectorDirection.getFrontOffsetX() * v,
						projectorDirection.getFrontOffsetY() * v,
						projectorDirection.getFrontOffsetZ() * v
				);
				surface.cornerEnd = bottomRight.subtract(
						projectorDirection.getFrontOffsetX() * v,
						projectorDirection.getFrontOffsetY() * v,
						projectorDirection.getFrontOffsetZ() * v
				);
				surface.orientation = orientation;
				surface.width = sizeFactorW * i;
				surface.height = sizeFactorH * i;

				return surface;
			} else if (solidCount != totalCount && hasBlocks) {
				return null;
			}
		}

		return null;
	}

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		Minecraft.getMinecraft().mcProfiler.startSection("projectors");

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.setTranslation(0, 0, 0);

		Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
		Vec3d cameraPos = EntityUtils.interpolate(cameraEntity, event.getPartialTicks());

		ICamera camera = new Frustum();
		camera.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		for (TileEntity tileEntity : Minecraft.getMinecraft().world.loadedTileEntityList) {
			if (tileEntity instanceof TileProjector) {
				LaserColor color = LaserColor.NONE;
				Orientation orientation = ((TileProjector) tileEntity).getOrientation();

				if (CharsetProjector.useLasers) {
					for (int d = 0; d < 6; d++) {
						LaserColor color2 = ((TileProjector) tileEntity).colors[d];
						if (color2 != null && color2 != LaserColor.NONE) {
							if (d == orientation.facing.getOpposite().ordinal()) {
								// not rendering anything - laser in the way
								color = LaserColor.NONE;
								break;
							} else {
								color = color.union(color2);
							}
						}
					}
				} else {
					color = ((TileProjector) tileEntity).redstoneLevel > 0 ? LaserColor.WHITE : LaserColor.NONE;
				}

				if (color != LaserColor.NONE) {
					ItemStack stack = ((TileProjector) tileEntity).getStack();
					IProjectorHandler<ItemStack> handler = CharsetProjector.getHandler(stack);
					if (handler == null) {
						continue;
					}

					Surface surface = getSurface(tileEntity.getWorld(), tileEntity.getPos(), orientation, 0.5f, handler.getAspectRatio(stack));
					if (surface == null) {
						continue;
					}

					if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(surface.cornerStart, surface.cornerEnd))) {
						continue;
					}

					surface.r = color.red ? 1.0f : 0.0f;
					surface.g = color.green ? 1.0f : 0.0f;
					surface.b = color.blue ? 1.0f : 0.0f;
					surface.a = 0.5f;

					if (!CharsetProjector.useLasers) {
						surface.a *= ((TileProjector) tileEntity).redstoneLevel / 15.0f;
					}

					EnumDyeColor dyeColor = null;

					BlockPos inFrontPos = tileEntity.getPos().offset(((TileProjector) tileEntity).getOrientation().facing);
					IBlockState state = tileEntity.getWorld().getBlockState(inFrontPos);
					if (state.getBlock() instanceof BlockStainedGlass) {
						dyeColor = state.getValue(BlockStainedGlass.COLOR);
					}

					if (dyeColor != null) {
						float[] v = ColorUtils.getDyeRgb(dyeColor);
						surface.r *= v[0];
						surface.g *= v[1];
						surface.b *= v[2];
					}

					surface.restoreGLColor();
					handler.render(stack, (IProjector) tileEntity, surface);
				}
			}
		}

		GlStateManager.popMatrix();

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.disableBlend();

		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
