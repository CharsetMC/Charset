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

package pl.asie.charset.module.optics.laser.blocks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.lib.block.ITileWrenchRotatable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.module.optics.laser.CharsetLaser;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.api.laser.LaserColor;

import javax.annotation.Nullable;

// down,west -> down-east

public class TilePrism extends TileLaserSourceBase implements IAxisRotatable, ITileWrenchRotatable {
	public enum FaceBehaviour {
		DIAGONAL,
		NON_DIAGONAL,
		PASS,
	};

	private final ILaserReceiver[] receivers = new ILaserReceiver[6];
	private final LaserColor[] sourceColors = new LaserColor[6];
	private Orientation orientation;

	private FaceBehaviour getFaceBehaviour(EnumFacing face) {
		if (face.getAxis() != orientation.facing.getAxis() && face.getAxis() != orientation.top.getAxis()) {
			return FaceBehaviour.PASS;
		} else {
			if (face == orientation.top || face == orientation.facing.getOpposite())
				return FaceBehaviour.NON_DIAGONAL;
			else
				return FaceBehaviour.DIAGONAL;
		}
	}

	private void recalcColors() {
		LaserColor[] oldColors = new LaserColor[6];
		for (int i = 0; i < 6; i++) {
			oldColors[i] = colors[i];
			colors[i] = LaserColor.NONE;
		}

		for (int i = 0; i < 6; i++) {
			LaserColor source = sourceColors[i];
			if (source != LaserColor.NONE) {
				EnumFacing face = EnumFacing.getFront(i);
				FaceBehaviour b = getFaceBehaviour(face);
				if (b == FaceBehaviour.PASS) {
					colors[i ^ 1] = colors[i ^ 1].union(source);
				} else {
					// TODO: Optimize

					boolean isRed = (source.ordinal() & 4) != 0;
					boolean isGreen = (source.ordinal() & 2) != 0;
					boolean isBlue = (source.ordinal() & 1) != 0;

					if (isGreen) {
						colors[i ^ 1] = colors[i ^ 1].union(LaserColor.GREEN);
					}

					if (isRed || isBlue) {
						for (EnumFacing target : EnumFacing.VALUES) {
							FaceBehaviour targetB = getFaceBehaviour(target);
							if (targetB != FaceBehaviour.PASS && face.getAxis() != target.getAxis()) {
								if (isRed && targetB != b) {
									colors[target.ordinal()] = colors[target.ordinal()].union(LaserColor.RED);
								}

								if (isBlue && targetB == b) {
									colors[target.ordinal()] = colors[target.ordinal()].union(LaserColor.BLUE);
								}
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < 6; i++) {
			if (colors[i] != oldColors[i]) {
				CharsetLaser.laserStorage.markLaserForUpdate(this, EnumFacing.getFront(i));
			}
		}
	}

	@Override
	public void invalidate(InvalidationType type) {
		super.invalidate(type);
		for (int i = 0; i < 6; i++) {
			sourceColors[i] = LaserColor.NONE;
		}
	}

	public TilePrism() {
		for (int i = 0; i < 6; i++) {
			final int _i = i;
			sourceColors[i] = LaserColor.NONE;
			receivers[i] = (color) -> {
				if (world.isRemote) {
					return;
				}

				if (sourceColors[_i] != color) {
					sourceColors[_i] = color;
					recalcColors();
				}
			};
		}
	}

	private void updateRotations() {
		recalcColors();
		markBlockForUpdate();
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		// TODO
		orientation = Orientation.fromDirection(EnumFacing.getDirectionFromEntityLiving(getPos(), placer));
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		orientation = Orientation.getOrientation(compound.getByte("o"));

		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound.setByte("o", (byte) orientation.ordinal());
		return compound;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing != null && (capability == CharsetLaser.LASER_RECEIVER)) || capability == Capabilities.AXIS_ROTATABLE || super.hasCapability(capability, facing);
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
				updateRotations();
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
		if (axis.getAxis() == orientation.facing.getAxis()) {
			newOrientation = Orientation.getOrientation(Orientation.fromDirection(axis).ordinal() & (~3) | (orientation.ordinal() & 3)).getNextRotationOnTop();
		} else if (axis.getAxis() == orientation.top.getAxis()) {
			newOrientation = Orientation.getOrientation(Orientation.fromDirection(axis).ordinal() & (~3) | (orientation.ordinal() & 3));
		} else {
			newOrientation = orientation.getPrevRotationOnFace().getNextRotationOnTop().getNextRotationOnFace();
		}

		changeOrientation(newOrientation, false);
		return true;
	}
}
