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

package pl.asie.charset.module.power.steam;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.scheduler.ScheduledEvent;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.module.power.mechanical.CharsetPowerMechanical;
import pl.asie.charset.module.power.steam.api.IMirror;
import pl.asie.charset.module.power.steam.api.IMirrorTarget;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class TileMirror extends TileBase implements IMirror {
	public static final int SEARCH_DISTANCE = 10;
	private static final double MAX_RADIUS_SQ = (SEARCH_DISTANCE-1.1)*(SEARCH_DISTANCE-1.1);

	private ItemMaterial material = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.IRON_BLOCK));
	private BlockPos targetPos = null;

	public ItemMaterial getMaterial() {
		return material;
	}

	private boolean loadMaterialFromNBT(NBTTagCompound compound) {
		ItemMaterial nm = ItemMaterialRegistry.INSTANCE.getMaterial(compound, "material");
		if (nm != null && nm != material) {
			material = nm;
			return true;
		} else {
			return false;
		}
	}

	private void saveMaterialToNBT(NBTTagCompound compound) {
		getMaterial().writeToNBT(compound, "material");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		MirrorChunkContainer.registerMirror(world, this);
		requestMirrorTargetRefresh();
	}

	@Override
	public void invalidate(InvalidationType type) {
		super.invalidate(type);
		if (type == InvalidationType.REMOVAL) {
			// chunk /unloading/ removes the container as well
			MirrorChunkContainer.unregisterMirror(world, this);
		}
	}

	@Override
	public boolean isMirrorActive() {
		return isMirrorValid() && hasSun();
	}

	protected boolean canSearchForTarget() {
		if (!world.canSeeSky(pos)) return false;
		if (MirrorChunkContainer.getHighestMirror(world, pos) != this) return false;
		return true;
	}

	protected boolean hasSun() {
		if (!canSearchForTarget()) return false;
		if (world.isRainingAt(pos)) return false;
		return world.getSunBrightnessFactor(0) > 0.7;
	}

	protected boolean hasTargetChanged(BlockPos oldTargetPos) {
		return oldTargetPos != targetPos && (oldTargetPos == null || !oldTargetPos.equals(pos));
	}

	protected void findTarget() {
		BlockPos oldTargetPos = targetPos;
		double targetDistance = Double.MAX_VALUE;
		TileEntity target = null;

		if (!isInvalid() && canSearchForTarget()) {
			for (int direction = 0; direction < 4; direction++) {
				for (int i = 0; i < SEARCH_DISTANCE * 2; i++) {
					BlockPos targetPos;
					switch (direction) {
						case 0:
						default:
							targetPos = new BlockPos(
									pos.getX() - SEARCH_DISTANCE + i,
									pos.getY(),
									pos.getZ() - SEARCH_DISTANCE
							);
							break;
						case 1:
							targetPos = new BlockPos(
									pos.getX() + SEARCH_DISTANCE,
									pos.getY(),
									pos.getZ() - SEARCH_DISTANCE + i
							);
							break;
						case 2:
							targetPos = new BlockPos(
									pos.getX() + SEARCH_DISTANCE - i,
									pos.getY(),
									pos.getZ() + SEARCH_DISTANCE
							);
							break;
						case 3:
							targetPos = new BlockPos(
									pos.getX() - SEARCH_DISTANCE,
									pos.getY(),
									pos.getZ() + SEARCH_DISTANCE - i
							);
							break;
					}

					RayTraceUtils.Result resultTmp = RayTraceUtils.getCollision(world, new Vec3d(pos).add(0.5, 0.5, 0.5), new Vec3d(targetPos).add(0.5, 0.5, 0.5), (checkPos) -> {
						IBlockState cstate = world.getBlockState(checkPos);
						return !(cstate.getBlock() instanceof BlockMirror) && cstate.getLightOpacity(world, checkPos) <= 0;
					});

					if (resultTmp.valid()) {
						double distTmp = pos.distanceSq(resultTmp.hit.getBlockPos());
						if (distTmp <= MAX_RADIUS_SQ && distTmp < targetDistance) {
							TileEntity tile = world.getTileEntity(resultTmp.hit.getBlockPos());
							if (tile != null && tile.hasCapability(CharsetPowerSteam.MIRROR_TARGET, null) && target != tile) {
								target = tile;
							}
						}
					}
				}
			}
		}

		if (target != null) {
			targetPos = target.getPos();
		} else {
			targetPos = null;
		}

		if (hasTargetChanged(oldTargetPos)) {
			if (oldTargetPos != null) {
				TileEntity oldTarget = world.getTileEntity(oldTargetPos);
				if (oldTarget != null && oldTarget.hasCapability(CharsetPowerSteam.MIRROR_TARGET, null)) {
					Objects.requireNonNull(oldTarget.getCapability(CharsetPowerSteam.MIRROR_TARGET, null)).unregisterMirror(this);
				}
			}

			if (target != null) {
				Objects.requireNonNull(target.getCapability(CharsetPowerSteam.MIRROR_TARGET, null)).registerMirror(this);
			}

			markBlockForUpdate();
		}
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		ItemStack stack = new ItemStack(CharsetPowerSteam.itemMirror, 1, 0);
		saveMaterialToNBT(ItemUtils.getTagCompound(stack, true));
		return stack;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		loadMaterialFromNBT(stack.getTagCompound());
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		loadMaterialFromNBT(compound);

		BlockPos oldTargetPos = targetPos;
		if (compound.hasKey("target", Constants.NBT.TAG_COMPOUND)) {
			targetPos = NBTUtil.getPosFromTag(compound.getCompoundTag("target"));
		} else {
			targetPos = null;
		}

		if (isClient && hasTargetChanged(oldTargetPos)) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		saveMaterialToNBT(compound);
		if (targetPos != null) {
			compound.setTag("target", NBTUtil.createPosTag(targetPos));
		}
		return compound;
	}

	@Override
	public boolean isMirrorValid() {
		return !isInvalid();
	}

	@Override
	public BlockPos getMirrorPos() {
		return getPos();
	}

	@Override
	public Optional<BlockPos> getMirrorTargetPos() {
		return Optional.ofNullable(targetPos);
	}

	@Override
	public float getMirrorStrength() {
		return 1;
		/* float avg = 0.75f;
		float diff = 0.25f;
		float val = avg;
		if (targetPos != null) {
			double mirrorRotation = MathHelper.atan2(
					targetPos.getX() - pos.getX(),
					targetPos.getZ() - pos.getZ()
			);
			double celestialAngle = world.getCelestialAngleRadians(0);
			double targetCelestialAngle = 0;
			double mul = 0;

			if (mirrorRotation >= 0) {
				// half PI = east-facing
				diff *= ((1 - Math.abs(mirrorRotation - (Math.PI / 2)) / (Math.PI / 2)));
				targetCelestialAngle = Math.PI * 1.75;

			} else {
				// half PI = west-facing
				diff *= ((1 - Math.abs(mirrorRotation + (Math.PI / 2)) / (Math.PI / 2)));
				targetCelestialAngle = Math.PI * 0.25;
			}

			double range = Math.PI * 0.5;
			double tcDiff = Math.abs(celestialAngle - targetCelestialAngle);
			if (tcDiff >= range) {
				mul = -1;
			} else {
				mul = 1 - ((tcDiff / range) * 2);
			}

			val += diff*mul;
		}
		return val; */
	}

	private ScheduledEvent event;

	@Override
	public void requestMirrorTargetRefresh() {
		if (event == null || event.hasExecuted()) {
			event = Scheduler.INSTANCE.in(world, 0, this::findTarget);
		}
	}
}
