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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.capability.TileCache;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.module.power.CharsetPower;
import pl.asie.charset.module.power.PowerCapabilities;
import pl.asie.charset.module.power.api.IPowerProducer;
import pl.asie.charset.module.power.api.IPowerConsumer;

import javax.annotation.Nullable;

public class TileAxle extends TileBase {
	protected class AxleSide implements IPowerProducer, IPowerConsumer {
		protected final EnumFacing facing;
		protected final int i;
		protected final TileCache cache;
		protected double forceReceived, lastDesiredForce;

		public AxleSide(int i, EnumFacing facing) {
			this.i = i;
			this.facing = facing;
			this.cache = new TileCache(
					world, pos.offset(facing.getOpposite())
			);
		}

		@Override
		public boolean isAcceptingForce() {
			if (powerOutputs[i ^ 1] != null && powerOutputs[i ^ 1].forceReceived != 0.0) return false;

			IPowerConsumer output = CapabilityHelper.get(
					PowerCapabilities.POWER_CONSUMER, cache.getTile(), facing
			);

			return output != null && output.isAcceptingForce();
		}

		@Override
		public double getDesiredForce() {
			if (powerOutputs[i ^ 1] != null && powerOutputs[i ^ 1].forceReceived != 0.0) return 0;

			IPowerConsumer output = CapabilityHelper.get(
					PowerCapabilities.POWER_CONSUMER, cache.getTile(), facing
			);

			return output != null ? output.getDesiredForce() : 0;
		}

		@Override
		public void setForce(double val) {
			IPowerConsumer output = CapabilityHelper.get(
					PowerCapabilities.POWER_CONSUMER, cache.getTile(), facing
			);

			if (output != null) {
				output.setForce(val);
			}

			double ds = getDesiredForce();
			if (forceReceived != val || lastDesiredForce != ds) {
				System.out.println(lastDesiredForce + " -> " + ds);
				forceReceived = val;
				lastDesiredForce = ds;
				markBlockForUpdate();
			}
		}

		public void onNeighborChanged(BlockPos pos) {
			if (pos.equals(TileAxle.this.pos.offset(facing))) {
				setForce(0.0);
				markBlockForUpdate();
			}
		}
	}

	public double rotSpeedClient;
	protected AxleSide[] powerOutputs = new AxleSide[2];
	protected ItemMaterial material = ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank");
	protected boolean rendered;

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

	public void onNeighborChanged(BlockPos pos) {
		for (int i = 0; i < 2; i++) {
			if (powerOutputs[i] != null) {
				powerOutputs[i].onNeighborChanged(pos);
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == PowerCapabilities.POWER_PRODUCER || capability == PowerCapabilities.POWER_CONSUMER) {
			EnumFacing.Axis axis = EnumFacing.Axis.values()[getBlockMetadata()];
			return facing != null && facing.getAxis() == axis;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == PowerCapabilities.POWER_PRODUCER || capability == PowerCapabilities.POWER_CONSUMER) {
			EnumFacing.Axis axis = EnumFacing.Axis.values()[getBlockMetadata()];
			if (facing != null && facing.getAxis() == axis) {
				EnumFacing.AxisDirection direction = facing.getAxisDirection();
				int i = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
				if (powerOutputs[i] == null) {
					powerOutputs[i] = new AxleSide(i, facing);
				}
				return (T) powerOutputs[i];
			} else {
				return null;
			}
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		powerOutputs[0] = powerOutputs[1] = null;
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		ItemStack stack = new ItemStack(CharsetPower.itemAxle, 1, 0);
		saveMaterialToNBT(ItemUtils.getTagCompound(stack, true));
		return stack;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		loadMaterialFromNBT(stack.getTagCompound());
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		boolean r = loadMaterialFromNBT(compound);
		if (/*(r || !rendered) && */isClient) {
			rotSpeedClient = compound.getFloat("rs");
			markBlockForRenderUpdate();
			rendered = true;
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		saveMaterialToNBT(compound);
		if (isClient) {
			double ds0 = powerOutputs[0] != null ? powerOutputs[0].getDesiredForce() : 0.0;
			double ds1 = powerOutputs[1] != null ? powerOutputs[1].getDesiredForce() : 0.0;
			if (ds0 != 0.0) {
				ds0 = powerOutputs[0].forceReceived / ds0;
			}
			if (ds1 != 0.0) {
				ds1 = powerOutputs[1].forceReceived / ds1;
			}

			System.out.println(pos + " " + Math.max(ds0, ds1));

			compound.setFloat("rs", (float) Math.max(ds0, ds1));
		}
		return compound;
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public ItemMaterial getMaterial() {
		if (material == null) {
			material = ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank");
		}
		return material;
	}
}
