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

package pl.asie.charset.lib.capability.redstone;

import pl.asie.charset.api.wires.IBundledReceiver;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class BundledReceiverCombiner implements Function<List<IBundledReceiver>, IBundledReceiver> {
	private class WrappedReceiver implements IBundledReceiver {
		private final Collection<IBundledReceiver> receiverSet;

		public WrappedReceiver(Collection<IBundledReceiver> receiverSet) {
			this.receiverSet = receiverSet;
		}

		@Override
		public void onBundledInputChange() {
			for (IBundledReceiver r : receiverSet) {
				r.onBundledInputChange();
			}
		}
	}

	@Override
	public IBundledReceiver apply(List<IBundledReceiver> iBundledReceivers) {
		return new WrappedReceiver(iBundledReceivers);
	}
}
