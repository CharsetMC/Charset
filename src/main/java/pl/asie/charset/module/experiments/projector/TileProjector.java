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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.lib.block.ITileWrenchRotatable;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.module.laser.blocks.TileCrystal;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;

public class TileProjector extends TileBase implements IAxisRotatable, IProjector, ITileWrenchRotatable {
	protected final LaserColor[] colors = new LaserColor[6];
	private final ILaserReceiver[] receivers = new ILaserReceiver[6];
	private ItemStack stack = ItemStack.EMPTY;
	private int page;
	private Orientation orientation = Orientation.FACE_NORTH_POINT_UP;

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		orientation = SpaceUtils.getOrientation(getWorld(), getPos(), placer, face, hitX, hitY, hitZ);
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		orientation = Orientation.getOrientation(compound.getByte("o"));
		page = compound.getInteger("p");
		stack = compound.hasKey("stack", Constants.NBT.TAG_COMPOUND) ? new ItemStack(compound.getCompoundTag("stack")) : ItemStack.EMPTY;

		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound.setByte("o", (byte) orientation.ordinal());
		compound.setInteger("p", page);
		if (!stack.isEmpty()) {
			ItemUtils.writeToNBT(stack, compound, "stack");
		}

		return compound;
	}

	public ItemStack getStack() {
		return stack;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public void onLoad() {
		super.onLoad();

		for (int i = 0; i < 6; i++) {
			final int _i = i;
			colors[i] = LaserColor.NONE;
			receivers[i] = colorHit -> {
				if (colors[_i ^ 1] != colorHit) {
					colors[_i ^ 1] = colorHit;
					markBlockForRenderUpdate();
				}
			};
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing != null && (capability == CharsetLaser.LASER_RECEIVER && receivers[facing.ordinal()] != null)) || capability == Capabilities.AXIS_ROTATABLE || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == Capabilities.AXIS_ROTATABLE) {
			return Capabilities.AXIS_ROTATABLE.cast(this);
		}

		if (facing != null) {
			if (capability == CharsetLaser.LASER_RECEIVER) {
				return CharsetLaser.LASER_RECEIVER.cast(receivers[facing.ordinal()]);
			}
		}

		return super.getCapability(capability, facing);
	}

	private boolean changeOrientation(Orientation newOrientation, boolean simulate) {
		if (orientation != newOrientation) {
			if (!simulate) {
				orientation = newOrientation;
				markBlockForUpdate();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean rotateAround(EnumFacing axis, boolean simulate) {
		return changeOrientation(orientation.rotateAround(axis), simulate);
	}

	@Override
	public boolean rotateWrench(EnumFacing axis) {
		Orientation newOrientation;
		if (axis == orientation.facing.getOpposite()) {
			newOrientation = orientation.getNextRotationOnFace();
		} else {
			newOrientation = Orientation.getOrientation(Orientation.fromDirection(axis.getOpposite()).ordinal() & (~3) | (orientation.ordinal() & 3));
		}

		changeOrientation(newOrientation, false);
		return true;
	}

	public boolean activate(EntityPlayer player, EnumFacing side, EnumHand hand) {
		EnumFacing rightFace = orientation.getNextRotationOnTop().facing;
		EnumFacing leftFace = orientation.getPrevRotationOnTop().facing;

		if (!stack.isEmpty() && player.getHeldItem(hand).isEmpty()) {
			IProjectorHandler<ItemStack> handler = CharsetProjector.getHandler(stack);
			if (handler != null) {
				int pc = handler.getPageCount(stack);
				if (pc > 1) {
					if (side == rightFace && page < (pc - 1)) {
						page++;
						markBlockForUpdate();
						return true;
					} else if (side == leftFace && page > 0) {
						page--;
						markBlockForUpdate();
						return true;
					}
				}
			}
		}

		if (side == orientation.top && hand == EnumHand.MAIN_HAND) {
			if (!stack.isEmpty()) {
				ItemUtils.spawnItemEntity(getWorld(),
					new Vec3d(getPos()).addVector(0.5F, 0.5F, 0.5F).add(new Vec3d(orientation.top.getDirectionVec()).scale(0.5F)),
					stack, 0, 0, 0, 0
				);
				stack = ItemStack.EMPTY;
				page = 0;
				markBlockForUpdate();
				return true;
			} else {
				ItemStack held = player.getHeldItem(hand);
				if (!held.isEmpty()) {
					stack = held.splitStack(1);
					page = 0;
					markBlockForUpdate();
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int getPage() {
		return page;
	}
}
