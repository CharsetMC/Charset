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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.module.laser.system.LaserBeam;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.module.laser.system.LaserSource;

import javax.annotation.Nullable;

public class TileCrystal extends TileLaserSourceBase {
	protected final LaserColor[] newColors = new LaserColor[6];
	private ILaserReceiver[] receivers = new ILaserReceiver[6];
	private LaserColor color;
	private long updateQueued = -1;
	private int hitMask = 0, newHitMask = 0;

	public TileCrystal() {
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		if (state.getBlock() == CharsetLaser.blockCrystal) {
			return new ItemStack(CharsetLaser.blockCrystal, 1, state.getValue(CharsetLaser.LASER_COLOR).ordinal());
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	protected LaserSource createLaserSource(int i) {
		return new LaserSource(this) {
			@Override
			public void updateBeam() {
				LaserColor c = LaserColor.NONE;
				if (hitMask == 0) {
					//c = colors[i].union(colors[i ^ 1]);
					c = colors[i];
				}

				if (c == LaserColor.NONE) {
					beam = null;
				} else if (beam == null || !beam.isValid() || !beam.getStart().equals(getPos()) || beam.getColor() != c) {
					beam = new LaserBeam(TileCrystal.this, EnumFacing.getFront(i), c);
				}
			}
		};
	}

	@Override
	public void invalidate() {
		super.invalidate();
		hitMask = 0;

		for (int i = 0; i < 6; i++) {
			receivers[i] = null;
		}
	}

	private void updateOpacity(EnumFacing facing, IBlockState oldState, LaserColor colorHit, boolean force) {
		if (!force) {
			if (oldState == null) {
				oldState = world.getBlockState(pos);
			}
			force = oldState.getValue(BlockCrystal.OPAQUE) != (newHitMask != 0);
		}

		if (force) {
			// System.out.println("queuing update " + pos);
			if (updateQueued != world.getTotalWorldTime()) {
				// System.out.println("queued");
				Scheduler.INSTANCE.in(world, 0, () -> {
					// System.out.println("hitMask change " + world.getTotalWorldTime() + " " + pos + " " + hitMask + "->" + newHitMask);
					IBlockState oldState2 = world.getBlockState(pos);
					if (oldState2.getBlock() instanceof BlockCrystal && oldState2.getValue(BlockCrystal.OPAQUE) != (newHitMask != 0)) {
						hitMask = newHitMask;
						// System.out.println("hitMask change indeed " + hitMask);
						IBlockState newState = oldState2.withProperty(BlockCrystal.OPAQUE, hitMask != 0);
						world.setBlockState(pos, newState);
						for (EnumFacing facing2 : EnumFacing.VALUES) {
							colors[facing2.ordinal()] = newColors[facing2.ordinal()];
							CharsetLaser.laserStorage.markLaserForUpdate(TileCrystal.this, facing2);
						}

						for (int i = 0; i < 6; i++) {
							receivers[i].onLaserUpdate(colors[i ^ 1]);
						}

						// rapid change detection
						if (hitMask != newHitMask) {
							((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, false,
									pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
									1, 0.1D, 0.1D, 0.1D, 0.05D);
						}
					}
				});
			}
			updateQueued = world.getTotalWorldTime();
		} else {
			hitMask = newHitMask;
			if (facing != null) {
				colors[facing.ordinal()] = colorHit;
				CharsetLaser.laserStorage.markLaserForUpdate(TileCrystal.this, facing);
			}
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();

		for (int i = 0; i < 6; i++) {
			final int _i = i;
			newColors[i] = LaserColor.NONE;
			receivers[i] = colorHit -> {
				if (world.isRemote) {
					return;
				}

				IBlockState oldState = world.getBlockState(pos);
				color = oldState.getValue(CharsetLaser.LASER_COLOR);

	     		if (hitMask == 0) {
					if (((colorHit.ordinal() | newColors[_i].ordinal()) & TileCrystal.this.color.ordinal()) == TileCrystal.this.color.ordinal()) {
						newHitMask |= (3 << (_i & 6));
					} else {
						newHitMask &= ~(3 << (_i & 6));
					}
				} else {
					if ((colorHit.ordinal() & TileCrystal.this.color.ordinal()) == TileCrystal.this.color.ordinal()) {
						newHitMask |= (1 << _i);
					} else {
						newHitMask &= ~(1 << _i);
					}
		        }

				// Uncomment for combining lasers
					/* LaserColor combined = LaserColor.NONE;
					for (int i = 0; i < 6; i++) {
						combined = combined.union(newColors[i]);
					}
					newHitMask = (combined.union(color) == combined) ? 1 : 0; */

				if (newColors[_i ^ 1] != colorHit) {
					newColors[_i ^ 1] = colorHit;
					updateOpacity(EnumFacing.getFront(_i ^ 1), oldState, colorHit, false);
				} else {
					updateOpacity(null, oldState, colorHit, false);
				}
			};
		}

		updateOpacity(null, null, null, true);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing != null && (capability == CharsetLaser.LASER_RECEIVER && receivers[facing.ordinal()] != null)) || super.hasCapability(capability, facing);
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
		if (oldState.getBlock() != newState.getBlock()) {
			return true;
		} else if (oldState.getBlock() instanceof BlockCrystal) {
			return oldState.getValue(CharsetLaser.LASER_COLOR) != newState.getValue(CharsetLaser.LASER_COLOR);
		} else {
			return false;
		}
	}
}
