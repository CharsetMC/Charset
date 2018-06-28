package pl.asie.charset.module.crafting.cauldron.fluid;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
			if (event.getObject().getItem() == Items.GLASS_BOTTLE || event.getObject().getItem() == Items.POTIONITEM) {
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
		if (container.getItem() == Items.POTIONITEM) {
			if (PotionUtils.getPotionFromItem(container) == PotionTypes.WATER) {
				return new FluidStack(FluidRegistry.WATER, CharsetCraftingCauldron.waterBottleSize);
			}

			FluidStack stack = new FluidStack(CharsetCraftingCauldron.liquidPotion, CharsetCraftingCauldron.waterBottleSize);
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
			} else if (resource.getFluid() == CharsetCraftingCauldron.liquidPotion) {
				if (doFill) {
					FluidStack newStack = resource.copy();
					newStack.amount = CharsetCraftingCauldron.waterBottleSize;
					resource.amount -= newStack.amount;
					container = new ItemStack(Items.POTIONITEM);
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
