package pl.asie.charset.module.tweak.improvedCauldron.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public final class CauldronContents {
	private final FluidStack fluidStack;
	private final ItemStack heldItem;
	private final ITextComponent response;

	public CauldronContents(ITextComponent response) {
		this.response = response;
		this.fluidStack = null;
		this.heldItem = ItemStack.EMPTY;
	}

	public CauldronContents(FluidStack fluidStack, ItemStack heldItem) {
		this.response = null;
		this.fluidStack = fluidStack;
		this.heldItem = heldItem;
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
