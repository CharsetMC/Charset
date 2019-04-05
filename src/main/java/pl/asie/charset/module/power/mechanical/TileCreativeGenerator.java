/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerProducer;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;

public class TileCreativeGenerator extends TileBase implements ITickable, IMechanicalPowerProducer {
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

	@Override
	public void update() {
		super.update();

		if (!world.isRemote) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				TileEntity tile = world.getTileEntity(pos.offset(facing.getOpposite()));
				if (tile != null && tile.hasCapability(Capabilities.MECHANICAL_CONSUMER, facing)) {
					IMechanicalPowerConsumer output = tile.getCapability(Capabilities.MECHANICAL_CONSUMER, facing);
					if (output.isAcceptingPower()) {
						output.setForce(1.0, 3.0);
					}
				}
			}
		}
	}
}
