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

package pl.asie.charset.module.power.electric;

import net.minecraft.util.ResourceLocation;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;

public class WireProviderElectric extends WireProvider {
	@Override
	public Wire create(IWireContainer container, WireFace location) {
		return new WireElectric(container, this, location);
	}

	@Override
	public float getWidth() {
		return 0.5f;
	}

	@Override
	public float getHeight() {
		return 0.25f;
	}

	@Override
	public boolean hasFreestandingWire() {
		return false;
	}

	@Override
	public ResourceLocation getTexturePrefix() {
		return new ResourceLocation("charset:blocks/power_cable/outer");
	}
}
