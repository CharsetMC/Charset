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

package pl.asie.charset.module.crafting.cauldron.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public final class CauldronContents {
	private final FluidStack fluidStack;
	private final ItemStack heldItem;
	private final Source source;
	private final ITextComponent response;

	public enum Source {
		HAND,
		ENTITY,
		UNKNOWN
	}

	public CauldronContents(ITextComponent response) {
		this.response = response;
		this.fluidStack = null;
		this.source = Source.UNKNOWN;
		this.heldItem = ItemStack.EMPTY;
	}

	public CauldronContents(FluidStack fluidStack, ItemStack heldItem) {
		this.response = null;
		this.source = Source.UNKNOWN;
		this.fluidStack = fluidStack;
		this.heldItem = heldItem;
	}

	public CauldronContents(Source source, FluidStack fluidStack, ItemStack heldItem) {
		this.response = null;
		this.source = source;
		this.fluidStack = fluidStack;
		this.heldItem = heldItem;
	}

	public Source getSource() {
		return source;
	}

	public FluidStack getFluidStack() {
		return fluidStack;
	}

	public ItemStack getHeldItem() {
		return heldItem;
	}

	public ITextComponent getResponse() {
		return response;
	}

	public boolean hasResponse() {
		return response != null;
	}

	public boolean hasFluidStack() {
		return fluidStack != null;
	}

	public boolean hasHeldItem() {
		return !heldItem.isEmpty();
	}
}
