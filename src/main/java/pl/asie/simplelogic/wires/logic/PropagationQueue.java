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

package pl.asie.simplelogic.wires.logic;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public final class PropagationQueue {
	public final boolean clearMode;
	private final Queue<Pair<PartWireSignalBase, Integer>> wires;
	private final Deque<PartWireSignalBase> wiresFinish = new LinkedList<>();

	public PropagationQueue(boolean clearMode) {
		this.clearMode = clearMode;
		this.wires = new LinkedList<>();
	}

	public void add(PartWireSignalBase wire, int color) {
		wires.add(Pair.of(wire, color));
	}

	public void propagate() {
		while (!wires.isEmpty()) {
			Pair<PartWireSignalBase, Integer> pair = wires.remove();
			PartWireSignalBase wire = pair.getLeft();
			wire.propagate(pair.getRight(), this);
			wiresFinish.push(wire);
		}

		while (!wiresFinish.isEmpty()) {
			PartWireSignalBase wire = wiresFinish.pop();

			if (clearMode) {
				PropagationQueue queue1 = new PropagationQueue(false);
				queue1.add(wire, -1);
				queue1.propagate();
			}

			wire.finishPropagation();
		}
	}
}
