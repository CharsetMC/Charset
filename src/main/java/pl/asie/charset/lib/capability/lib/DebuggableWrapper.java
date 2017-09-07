/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib.capability.lib;

import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class DebuggableWrapper implements Function<List<IDebuggable>, IDebuggable> {
	@Override
	public IDebuggable apply(List<IDebuggable> iDebuggables) {
		return new Wrapped(iDebuggables);
	}

	private class Wrapped implements IDebuggable {
		private final Collection<IDebuggable> receivers;

		Wrapped(Collection<IDebuggable> receivers) {
			this.receivers = receivers;
		}

		@Override
		public void addDebugInformation(List<String> stringList, Side side) {
			for (IDebuggable debug : receivers)
				debug.addDebugInformation(stringList, side);
		}
	}
}