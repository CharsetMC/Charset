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

package pl.asie.charset.module.crafting.cauldron.fluid;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PotionFluidContainerItem implements IFluidHandlerItem, ICapabilityProvider {
	public static class Provider {
		public static final ResourceLocation LOCATION = new ResourceLocation("charset", "potion_fluid_container");
		public static CapabilityProviderFactory<IFluidHandlerItem> PROVIDER;

		@SubscribeEvent
		public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
			if (event.getObject().getItem() == Items.GLASS_BOTTLE || event.getObject().getItem() == Items.POTIONITEM
					|| event.getObject().getItem() == Items.SPLASH_POTION || event.getObject().getItem() == Items.LINGERING_POTION) {
				if (PROVIDER == null) {
					PROVIDER = new CapabilityProviderFactory<>(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
				}

				event.addCapability(LOCATION, PROVIDER.create(new PotionFluidContainerItem(event.getObject())));
			}
		}
	}

	private ItemStack container = ItemStack.EMPTY;

	public PotionFluidContainerItem() {

	}

	public PotionFluidContainerItem(ItemStack container) {
		this.container = container;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
				? CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this)
				: null;
	}

	@Nonnull
	@Override
	public ItemStack getContainer() {
		return container;
	}

	public FluidStack getFluid() {
		if (container.getItem() instanceof ItemPotion) {
			if (PotionUtils.getPotionFromItem(container) == PotionTypes.WATER) {
				return new FluidStack(FluidRegistry.WATER, CharsetCraftingCauldron.waterBottleSize);
			}

			Fluid fluid;
			if (container.getItem() == Items.POTIONITEM) {
				fluid = CharsetCraftingCauldron.liquidPotion;
			} else if (container.getItem() == Items.SPLASH_POTION) {
				fluid = CharsetCraftingCauldron.liquidSplashPotion;
			} else if (container.getItem() == Items.LINGERING_POTION) {
				fluid = CharsetCraftingCauldron.liquidLingeringPotion;
			} else {
				return null;
			}

			FluidStack stack = new FluidStack(fluid, CharsetCraftingCauldron.waterBottleSize);
			FluidPotion.copyFromPotionItem(stack, container);
			return stack;
		} else {
			// Items.GLASS_BOTTLE, ...
			return null;
		}
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new FluidTankProperties[] { new FluidTankProperties(getFluid(), CharsetCraftingCauldron.waterBottleSize) };
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (container.getCount() > 1 || container.getItem() != Items.GLASS_BOTTLE) {
			return 0;
		}

		if (resource.amount >= CharsetCraftingCauldron.waterBottleSize) {
			if (resource.getFluid().getName().equals("water")) {
				if (doFill) {
					resource.amount -= CharsetCraftingCauldron.waterBottleSize;
					container = new ItemStack(Items.POTIONITEM);
					PotionUtils.addPotionToItemStack(container, PotionTypes.WATER);
				}

				return CharsetCraftingCauldron.waterBottleSize;
			} else if (resource.getFluid() instanceof FluidPotion) {
				if (doFill) {
					FluidStack newStack = resource.copy();
					newStack.amount = CharsetCraftingCauldron.waterBottleSize;
					resource.amount -= newStack.amount;
					container = new ItemStack(FluidPotion.getPotionItem(newStack.getFluid()));
					FluidPotion.copyToPotionItem(container, newStack);
				}

				return CharsetCraftingCauldron.waterBottleSize;
			}
		}

		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (container.getCount() > 1) {
			return null;
		}

		FluidStack contained = getFluid();
		if (contained != null && resource.isFluidEqual(contained) && resource.amount >= contained.amount) {
			if (doDrain) {
				container = new ItemStack(Items.GLASS_BOTTLE);
			}
			return contained;
		}
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (container.getCount() > 1) {
			return null;
		}

		FluidStack contained = getFluid();
		if (contained != null && maxDrain >= contained.amount) {
			if (doDrain) {
				container = new ItemStack(Items.GLASS_BOTTLE);
			}
			return contained;
		}
		return null;
	}
}
