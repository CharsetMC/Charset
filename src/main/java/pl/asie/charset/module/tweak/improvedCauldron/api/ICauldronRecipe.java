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

package pl.asie.charset.module.tweak.improvedCauldron.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

@FunctionalInterface
public interface ICauldronRecipe {
	/**
	 * Apply the recipe.
	 * @param contents The current contents of the cauldron.
	 * @return A non-empty optional if the recipe matches; an empty optional if the recipe does not match.
	 */
	Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents);
}
