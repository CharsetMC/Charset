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

package pl.asie.simplelogic.gates.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponent;

public interface IGateContainer {
	World getGateWorld();
	BlockPos getGatePos();

	void scheduleTick(int value);
	default void scheduleTick() {
		scheduleTick(2);
	}

	byte[] getBundledInput(EnumFacing facing);
	boolean updateRedstoneInput(byte[] valueCache, EnumFacing facing);
	default boolean updateRedstoneInputs(byte[] valueCache) {
		boolean changed = false;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			changed |= updateRedstoneInput(valueCache, facing);
		}

		return changed;
	}

	Notice createNotice(NotificationComponent component);
	void markGateChanged();
	void openGUI(EntityPlayer playerIn);
	void propagateOutputs();
}
