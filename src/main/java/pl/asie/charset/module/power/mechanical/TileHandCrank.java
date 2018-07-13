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
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerProducer;
import pl.asie.charset.lib.CharsetSounds;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;

public class TileHandCrank extends TileBase implements ITickable, IMechanicalPowerProducer {
	public final TraitMechanicalRotation ROTATION;

	private static final double EPSILON = 0.001;
	private static final double DROPOFF = 0.035;
	private static final double MAXIMUM = 2.0;
	private double force;

	public TileHandCrank() {
		registerTrait("rot", ROTATION = new TraitMechanicalRotation());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (capability == Capabilities.MECHANICAL_PRODUCER && facing != null) || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == Capabilities.MECHANICAL_PRODUCER) {
			return Capabilities.MECHANICAL_PRODUCER.cast(this);
		} else {
			return super.getCapability(capability, facing);
		}
	}

	public double getForce() {
		return force;
	}

	protected double getAdjustedForce() {
		return Math.min(MAXIMUM, force);
	}

	protected boolean updateForce() {
		double oldForce = getAdjustedForce();
		force = force - DROPOFF;
		if (force <= EPSILON) {
			force = 0D;
		}

		ROTATION.tick(getAdjustedForce());
		return oldForce != getAdjustedForce();
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		force = compound.getDouble("f");
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		compound.setDouble("f", force);
		return compound;
	}

	@Override
	public void update() {
		super.update();
		if (updateForce()) {
			if (!world.isRemote) {
				EnumFacing facing = getFacing();
				TileEntity tile = world.getTileEntity(pos.offset(facing.getOpposite()));
				if (tile != null && tile.hasCapability(Capabilities.MECHANICAL_CONSUMER, facing)) {
					IMechanicalPowerConsumer output = tile.getCapability(Capabilities.MECHANICAL_CONSUMER, facing);
					if (output.isAcceptingPower()) {
						output.setForce(getAdjustedForce(), 0.25);
					}
				}
			}
		}
	}

	public EnumFacing getFacing() {
		IBlockState state = getWorld().getBlockState(getPos());
		if (!(state.getBlock() instanceof BlockHandCrank)) {
			return null;
		} else {
			return state.getValue(Properties.FACING);
		}
	}

	public void onActivated(EntityPlayer playerIn) {
		force = Math.min(MAXIMUM * 1.625, force + 1D);

		world.playSound(playerIn,
				pos.getX() + 0.5 + getFacing().getFrontOffsetX() * 0.25,
				pos.getY() + 0.5 + getFacing().getFrontOffsetY() * 0.25,
				pos.getZ() + 0.5 + getFacing().getFrontOffsetZ() * 0.25,
				CharsetSounds.BLOCK_CRANK,
				SoundCategory.BLOCKS,
				1f, 0.875f + (float) (force * 0.0625 / MAXIMUM) + (float) Math.random()*0.01f);

		markDirty();
		markBlockForUpdate();
	}
}
