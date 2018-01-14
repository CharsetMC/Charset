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

package pl.asie.charset.module.experiments.projector;

import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.Orientation;

public class ProjectorCacheInfo implements IRenderComparable<ProjectorCacheInfo> {
	final Orientation orientation;

	public ProjectorCacheInfo(Orientation o) {
		this.orientation = o;
	}

	public static ProjectorCacheInfo from(TileProjector projector) {
		return new ProjectorCacheInfo(projector.getOrientation());
	}

	public static ProjectorCacheInfo from(ItemStack is) {
		return new ProjectorCacheInfo(Orientation.FACE_NORTH_POINT_UP);
	}

	@Override
	public boolean renderEquals(ProjectorCacheInfo other) {
		return other.orientation == orientation;
	}

	@Override
	public int renderHashCode() {
		return orientation.hashCode();
	}
}
