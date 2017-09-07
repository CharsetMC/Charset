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

package pl.asie.charset.module.laser.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.api.laser.LaserColor;

import javax.annotation.Nullable;

public class TileReflector extends TileLaserSourceBase {
	private final ILaserReceiver[] receivers = new ILaserReceiver[6];
	private LaserColor[] bouncedColors = new LaserColor[6];
	private LaserColor[] passedColors = new LaserColor[6];
	private LaserColor[] sourceColors = new LaserColor[6];
	private LaserColor color = LaserColor.NONE;

	private void updateColor(int ri) {
		LaserColor oldColor = colors[ri];
		colors[ri] = passedColors[ri].union(bouncedColors[ri]);
		if (colors[ri] != oldColor) {
			CharsetLaser.laserStorage.markLaserForUpdate(TileReflector.this, EnumFacing.getFront(ri));
		}
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		if (state.getBlock() == CharsetLaser.blockReflector) {
			return new ItemStack(CharsetLaser.blockReflector, 1, color.ordinal() | (state.getValue(BlockReflector.SPLITTER) ? 8 : 0));
		} else {
			return new ItemStack(CharsetLaser.blockReflector, 1, color.ordinal());
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (int i = 0; i < 6; i++) {
			bouncedColors[i] = LaserColor.NONE;
			passedColors[i] = LaserColor.NONE;
			sourceColors[i] = LaserColor.NONE;
		}
	}

	public void updateSourceColor(int i) {
		IBlockState state = world.getBlockState(pos);
		EnumFacing rotation = state.getValue(BlockReflector.ROTATION);
		if ((rotation.ordinal() & (~1)) != (i & (~1))) {
			boolean isSplitter = state.getValue(BlockReflector.SPLITTER);

			LaserColor colorFiltered = sourceColors[i].intersection(this.color);
			LaserColor colorRemaining = isSplitter ? sourceColors[i] : sourceColors[i].difference(this.color);

			int ri = BlockReflector.getTargetFacing(EnumFacing.getFront(i), rotation).ordinal();
			if (colorFiltered != bouncedColors[ri]) {
				bouncedColors[ri] = colorFiltered;
				updateColor(ri);
			}
			ri = i ^ 1;
			if (colorRemaining != passedColors[ri]) {
				passedColors[ri] = colorRemaining;
				updateColor(ri);
			}
		} else {
			bouncedColors[i ^ 1] = LaserColor.NONE;
			if (passedColors[i ^ 1] != sourceColors[i]) {
				passedColors[i ^ 1] = sourceColors[i];
				updateColor(i ^ 1);
			}
		}
	}

	public TileReflector() {
		for (int i = 0; i < 6; i++) {
			final int _i = i;
			sourceColors[i] = LaserColor.NONE;
			bouncedColors[i] = LaserColor.NONE;
			passedColors[i] = LaserColor.NONE;
			receivers[i] = (color) -> {
				sourceColors[_i] = color;
				updateSourceColor(_i);
			};
		}
	}

	public void updateRotations() {
		for (int i = 0; i < 6; i++) {
			bouncedColors[i] = LaserColor.NONE;
			passedColors[i] = LaserColor.NONE;
			colors[i] = LaserColor.NONE;
		}
		for (int i = 0; i < 6; i++) {
			updateSourceColor(i);
			CharsetLaser.laserStorage.markLaserForUpdate(TileReflector.this, EnumFacing.getFront(i));
		}
		markBlockForUpdate();
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
		color = LaserColor.VALUES[stack.getItemDamage() & 7];
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		color = LaserColor.VALUES[compound.getByte("color")];

		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound.setByte("color", (byte) color.ordinal());
		return compound;
	}

	public LaserColor getColor() {
		return color;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing != null && (capability == CharsetLaser.LASER_RECEIVER)) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (facing != null) {
			if (capability == CharsetLaser.LASER_RECEIVER) {
				return CharsetLaser.LASER_RECEIVER.cast(receivers[facing.ordinal()]);
			}
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		updateRotations();
	}
}
