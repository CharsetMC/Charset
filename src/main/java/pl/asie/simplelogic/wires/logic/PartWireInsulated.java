/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.simplelogic.wires.logic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.api.wires.IWireInsulated;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.simplelogic.wires.OldWireUtils;

import javax.annotation.Nonnull;

public class PartWireInsulated extends PartWireNormal implements IWireInsulated {
	public PartWireInsulated(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	@Override
	protected int getRedstoneLevel(IBlockAccess world, BlockPos pos, WireFace location) {
		return OldWireUtils.getInsulatedWireLevel(world, pos, location, getColor());
	}

	@Override
	protected void onSignalChanged(int color) {
		if (getContainer().world() != null && getContainer().pos() != null && !getContainer().world().isRemote) {
			if (color == getColor() || color == -1) {
				propagate(color);
			}
		}
	}

	@Override
	public int getWireColor() {
		return getColor();
	}
}
