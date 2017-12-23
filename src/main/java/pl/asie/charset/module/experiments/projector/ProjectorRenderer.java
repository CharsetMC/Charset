/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.experiments.projector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.lib.utils.MethodHandleHelper;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RayTraceUtils;
import scala.xml.dtd.MIXED;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProjectorRenderer {
	private static MethodHandle MAP_DATA_LOCATION_GETTER;

	public static class Surface {
		public Vec3d cornerStart, cornerEnd;
		public EnumFacing direction;
		public List<BlockPos> particlePos = new ArrayList<>();
		public float[] uvValues = {
				0.0f, 0.0f,
				0.0f, 1.0f,
				1.0f, 1.0f,
				1.0f, 0.0f
		};
	}

	public Surface getSurface(World world, BlockPos pos, Orientation orientation, float sizeFactor, float aspectRatio) {
		Surface surface = new Surface();
		EnumFacing projectorDirection = orientation.facing;
		int maxDistance = 16;

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
					Vec3d axisVec = new Vec3d(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis).getDirectionVec());
					topLeft = topLeft.subtract(axisVec.scale(sizeFactor * i / (axis == orientation.top.getAxis() ? aspectRatio : 1)));
					bottomRight = bottomRight.add(axisVec.scale(sizeFactor * i / (axis == orientation.top.getAxis() ? aspectRatio : 1)));
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

			for (BlockPos checkPos : BlockPos.getAllInBox(topLeftPos, bottomRightPos)) {
				totalCount++;
				IBlockState state = world.getBlockState(checkPos);
				if (state.getBlock().isAir(state, world, pos) || state.getMaterial().isLiquid() || state.getBlock() instanceof IFluidBlock) {
					isSurface = false;
				} else {
					if (state.isSideSolid(world, pos, projectorDirection.getOpposite())) {
						solidCount++;
					}
				}
			}

			if (!isSurface && solidCount > 0) {
				return null;
			} else if (isSurface && solidCount == totalCount) {
				surface.cornerStart = topLeft.subtract(
						projectorDirection.getFrontOffsetX() * 1.001f,
						projectorDirection.getFrontOffsetY() * 1.001f,
						projectorDirection.getFrontOffsetZ() * 1.001f
				);
				surface.cornerEnd = bottomRight.subtract(
						projectorDirection.getFrontOffsetX() * 1.001f,
						projectorDirection.getFrontOffsetY() * 1.001f,
						projectorDirection.getFrontOffsetZ() * 1.001f
				);
				surface.direction = projectorDirection.getOpposite();
				for (int j = 0; j < orientation.getRotation(); j++) {
					float t = surface.uvValues[0];
					surface.uvValues[0] = surface.uvValues[2];
					surface.uvValues[2] = surface.uvValues[4];
					surface.uvValues[4] = surface.uvValues[6];
					surface.uvValues[6] = t;

					t = surface.uvValues[1];
					surface.uvValues[1] = surface.uvValues[3];
					surface.uvValues[3] = surface.uvValues[5];
					surface.uvValues[5] = surface.uvValues[7];
					surface.uvValues[7] = t;
				}

				return surface;
			} else {
				/* for (BlockPos pos2 : BlockPos.getAllInBox(topLeftPos, bottomRightPos)) {
					if (Math.random() < (1 << Minecraft.getMinecraft().gameSettings.particleSetting)/100f) {
						surface.particlePos.add(pos2);
					}
				} */
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
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		EntityPlayer player = Minecraft.getMinecraft().player;
		double cameraX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * event.getPartialTicks());
		double cameraY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * event.getPartialTicks());
		double cameraZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * event.getPartialTicks());

		worldrenderer.setTranslation(-cameraX, -cameraY, -cameraZ);

		for (TileEntity tileEntity : Minecraft.getMinecraft().world.loadedTileEntityList) {
			if (tileEntity instanceof TileProjector) {
				LaserColor color = LaserColor.NONE;
				Orientation orientation = ((TileProjector) tileEntity).getOrientation();

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

				if (color != LaserColor.NONE) {
					boolean foundTex = false;

					ItemStack stack = ((TileProjector) tileEntity).getStack();
					if (stack.getItem() instanceof ItemMap) {
						MapData mapData = ((ItemMap) stack.getItem()).getMapData(stack, tileEntity.getWorld());
						if (mapData != null && mapData.mapName != null) {
							MapItemRenderer mapItemRenderer = Minecraft.getMinecraft().entityRenderer.getMapItemRenderer();
							mapItemRenderer.updateMapTexture(mapData);

							Object o = mapItemRenderer.getMapInstanceIfExists(mapData.mapName);
							if (o != null) {
								if (MAP_DATA_LOCATION_GETTER == null) {
									MAP_DATA_LOCATION_GETTER = MethodHandleHelper.findFieldGetter(o.getClass(), "location", "field_148240_d");
								}

								try {
									Minecraft.getMinecraft().getTextureManager().bindTexture((ResourceLocation) MAP_DATA_LOCATION_GETTER.invoke(o));
									foundTex = true;
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
						}
					}

					if (!foundTex) {
						continue;
					}

					Surface surface = getSurface(tileEntity.getWorld(), tileEntity.getPos(), orientation, 0.5f, 1.0f);
					if (surface == null) {
						continue;
					}

					for (BlockPos particlePos : surface.particlePos) {
						tileEntity.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
								particlePos.getX() + 0.5,
								particlePos.getY() + 0.5,
								particlePos.getZ() + 0.5,
								0, 0, 0);
					}

					double[] data = {
							surface.cornerStart.y,
							surface.cornerEnd.y,
							surface.cornerStart.z,
							surface.cornerEnd.z,
							surface.cornerStart.x,
							surface.cornerEnd.x
					};

					worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

					EnumFaceDirection efd = EnumFaceDirection.getFacing(surface.direction);
					for (int i = 0; i < 4; i++) {
						EnumFaceDirection.VertexInformation vi = efd.getVertexInformation(i);
						worldrenderer.pos(data[vi.xIndex], data[vi.yIndex], data[vi.zIndex]).tex(surface.uvValues[i * 2], surface.uvValues[i * 2 + 1])
								.color(color.red ? 255 : 0, color.green ? 255 : 0, color.blue ? 255 : 0, 128).endVertex();
					}
					tessellator.draw();
				}
			}
		}

		worldrenderer.setTranslation(0,0,0);

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.disableBlend();

		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
