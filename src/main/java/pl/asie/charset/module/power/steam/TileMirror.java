package pl.asie.charset.module.power.steam;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.module.power.steam.api.IMirror;
import pl.asie.charset.module.power.steam.api.IMirrorTarget;

import java.util.Optional;

public class TileMirror extends TileBase implements IMirror, ITickable {
	private BlockPos targetPos = null;

	protected boolean hasSun() {
		if (world.isRainingAt(pos)) return false;
		if (world.getLightFor(EnumSkyBlock.SKY, pos) < 0xF) return false;
		// TODO: mirror coverings
		return world.getSunBrightnessFactor(0) > 0.7;
	}

	@Override
	public void update() {
		super.update();
		if (!world.isRemote) {
			findTarget();
		}
	}

	protected boolean hasTargetChanged(BlockPos oldTargetPos) {
		return oldTargetPos != targetPos && (oldTargetPos == null || !oldTargetPos.equals(pos));
	}

	protected void findTarget() {
		if (!hasSun()) {
			targetPos = null;
			return;
		}

		int search_distance = 10;
		double maxRadiusSq = 8.9*8.9;
		double targetDistance = Double.MAX_VALUE;
		TileEntity target = null;

		for (int direction = 0; direction < 4; direction++) {
			for (int i = 0; i < search_distance * 2; i++) {
				BlockPos targetPos;
				switch (direction) {
					case 0:
					default:
						targetPos = new BlockPos(
								pos.getX() - search_distance + i,
								pos.getY(),
								pos.getZ() - search_distance
						);
						break;
					case 1:
						targetPos = new BlockPos(
								pos.getX() + search_distance,
								pos.getY(),
								pos.getZ() - search_distance + i
						);
						break;
					case 2:
						targetPos = new BlockPos(
								pos.getX() + search_distance - i,
								pos.getY(),
								pos.getZ() + search_distance
						);
						break;
					case 3:
						targetPos = new BlockPos(
								pos.getX() - search_distance,
								pos.getY(),
								pos.getZ() + search_distance - i
						);
						break;
				}

				RayTraceUtils.Result resultTmp = RayTraceUtils.getCollision(world, new Vec3d(pos).addVector(0.5, 0.5, 0.5), new Vec3d(targetPos).addVector(0.5, 0.5, 0.5), (checkPos) -> {
					IBlockState cstate = world.getBlockState(checkPos);
					return cstate.getMaterial() == Material.GLASS || cstate.getMaterial() == Material.LEAVES;
				});

				if (resultTmp.valid()) {
					double distTmp = pos.distanceSq(resultTmp.hit.getBlockPos());
					if (distTmp <= maxRadiusSq && distTmp < targetDistance) {
						TileEntity tile = world.getTileEntity(resultTmp.hit.getBlockPos());
						if (tile instanceof IMirrorTarget && target != tile) {
							target = tile;
						}
					}
				}
			}
		}

		BlockPos oldTargetPos = targetPos;

		if (target != null) {
			targetPos = target.getPos();
		} else {
			targetPos = null;
		}

		if (hasTargetChanged(oldTargetPos)) {
			if (oldTargetPos != null) {
				TileEntity oldTarget = world.getTileEntity(oldTargetPos);
				if (oldTarget instanceof IMirrorTarget) {
					((IMirrorTarget) oldTarget).unregisterMirror(this);
				}
			}

			if (target != null) {
				((IMirrorTarget) target).registerMirror(this);
			}

			markBlockForUpdate();
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);

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
	public Optional<BlockPos> getMirrorTargetPos() {
		return Optional.ofNullable(targetPos);
	}

	@Override
	public void requestMirrorTargetRefresh() {
		findTarget();
	}
}
