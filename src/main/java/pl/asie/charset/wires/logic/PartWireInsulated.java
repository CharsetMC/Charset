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

package pl.asie.charset.wires.logic;

import mcmultipart.multipart.IMultipartContainer;
import pl.asie.charset.api.wires.IWireInsulated;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.wires.WireUtils;

public class PartWireInsulated extends PartWireNormal implements IWireInsulated {
	@Override
	protected int getRedstoneLevel(IMultipartContainer container, WireFace location) {
		return WireUtils.getInsulatedWireLevel(container, location, getColor());
	}

	@Override
	protected void onSignalChanged(int color) {
		if (getWorld() != null && !getWorld().isRemote) {
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
