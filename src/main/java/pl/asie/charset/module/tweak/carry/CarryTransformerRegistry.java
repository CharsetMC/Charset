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

package pl.asie.charset.module.tweak.carry;

import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CarryTransformerRegistry {
	public static final CarryTransformerRegistry INSTANCE = new CarryTransformerRegistry();
	private final Set<ICarryTransformer<Entity>> entityTransformers;
	private final Set<ICarryTransformer<Entity>> entityTransformersView;

	public CarryTransformerRegistry() {
		entityTransformers = new HashSet<>();
		entityTransformersView = Collections.unmodifiableSet(entityTransformers);
	}

	public void registerEntityTransformer(ICarryTransformer<Entity> transformer) {
		entityTransformers.add(transformer);
	}

	public Set<ICarryTransformer<Entity>> getEntityTransformers() {
		return entityTransformersView;
	}
}
