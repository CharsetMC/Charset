/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.power.mechanical.render;

import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.model.ModelColorHandler;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.utils.RenderUtils;

public class GearboxColorHandler extends ModelColorHandler<GearboxCacheInfo> {
	public static final GearboxColorHandler INSTANCE = new GearboxColorHandler();

	public GearboxColorHandler() {
		super(ModelGearbox.INSTANCE);
	}

	@Override
	public int colorMultiplier(GearboxCacheInfo info, int tintIndex) {
		if (tintIndex == 0) {
			return RenderUtils.getAverageColor(info.plank, RenderUtils.AveragingMode.FULL);
		} else if (tintIndex == 1) {
			return 0xFF404040;
		} else {
			return -1;
		}
	}
}
