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
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.module.laser.system.LaserBeam;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.module.laser.system.LaserSource;

import javax.annotation.Nullable;

/* @Optional.Interface(modid = "albedo", iface = "elucent.albedo.lighting.ILightProvider") */
public class TileJar extends TileBase /* implements ILightProvider */ {
	private final ILaserReceiver[] receivers = new ILaserReceiver[6];
	private LaserColor[] colors = new LaserColor[6];
	private LaserColor outputColor = LaserColor.NONE;
	private LaserSource source;

	private void recalculateOutputColor() {
		LaserColor newOutputColor = LaserColor.NONE;

		EnumFacing jarFacing = getJarFacing();
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing.getAxis() != jarFacing.getAxis()) {
				newOutputColor = newOutputColor.union(colors[facing.ordinal()]);
			}
		}

		if (newOutputColor != LaserColor.NONE) {
			outputColor = newOutputColor;
			markBlockForUpdate();
			CharsetLaser.laserStorage.markLaserForUpdate(TileJar.this, getJarFacing());
		}
	}

	public TileJar() {
		for (int i = 0; i < 6; i++) {
			final int _i = i;
			colors[i] = LaserColor.NONE;
			receivers[i] = (color) -> {
				colors[_i] = color;
				recalculateOutputColor();
			};
		}

		source = new LaserSource(this) {
			@Override
			public void updateBeam() {
				EnumFacing jarFacing = getJarFacing();
				if (outputColor == LaserColor.NONE) {
					beam = null;
				} else if (beam == null || !beam.isValid() || !beam.getStart().equals(getPos())  || beam.getColor() != outputColor || beam.getDirection() != jarFacing) {
					beam = new LaserBeam(TileJar.this, jarFacing, outputColor);
				}
			}
		};
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (int i = 0; i < 6; i++) {
			colors[i] = LaserColor.NONE;
		}
	}

	public EnumFacing getJarFacing() {
		return world.getBlockState(pos).getValue(Properties.FACING);
	}

	public void updateRotations() {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == CharsetLaser.blockJar) {
			recalculateOutputColor();
			CharsetLaser.laserStorage.markLaserForUpdate(TileJar.this, getJarFacing());
		} else {
			Scheduler.INSTANCE.in(world, 0, this::updateRotations);
		}
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		return new ItemStack(CharsetLaser.blockJar, 1, outputColor.ordinal());
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
		outputColor = LaserColor.VALUES[stack.getItemDamage() & 7];
	}

	@Override
	public void onLoad() {
		CharsetLaser.laserStorage.registerLaserSources(getWorld(), getPos());
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		outputColor = LaserColor.VALUES[compound.getByte("color")];

		if (isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound.setByte("color", (byte) outputColor.ordinal());
		return compound;
	}

	public LaserColor getColor() {
		return outputColor;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (facing != null) {
			if (capability == CharsetLaser.LASER_RECEIVER) {
				return true;
			}

			if (capability == CharsetLaser.LASER_SOURCE) {
				EnumFacing jarFacing = getJarFacing();
				if (facing == jarFacing) {
					return true;
				}
			}
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (facing != null) {
			if (capability == CharsetLaser.LASER_RECEIVER) {
				return CharsetLaser.LASER_RECEIVER.cast(receivers[facing.ordinal()]);
			}

			if (capability == CharsetLaser.LASER_SOURCE) {
				EnumFacing jarFacing = getJarFacing();
				if (facing == jarFacing) {
					return CharsetLaser.LASER_SOURCE.cast(source);
				}
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
/*
	@Nullable
	@Override
	@Optional.Method(modid = "albedo")
	@SideOnly(Side.CLIENT)
	public Light provideLight() {
		int c = CharsetLaser.LASER_COLORS[outputColor.ordinal()];
		return new Light(getPos().getX() + 0.5f, getPos().getY() + 0.5f, getPos().getZ() + 0.5f,
				(((c>>16)&0xFF)/255.0f), (((c>>8)&0xFF)/255.0f), (((c)&0xFF)/255.0f), 1.0f,
				1.0f);
	}
*/
}
