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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.block.Trait;

import javax.annotation.Nullable;

public class TraitMechanicalRotation extends Trait {
	private double rotation;
	private double force;

	public double getRotation() {
		return rotation;
	}

	public double getRotation(float partialTicks) {
		return rotation + (partialTicks * getForce());
	}

	public double getForce() {
		return force;
	}

	public void reset() {
		rotation = 0;
		force = 0;
	}

	public void tick(double val) {
		rotation += force;
		force = val;
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		rotation = compound.getDouble("r");
		force = compound.getDouble("f");
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setDouble("r", rotation);
		compound.setDouble("f", force);
		return compound;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return null;
	}
}
