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

package pl.asie.charset.module.crafting.cauldron.fluid;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.charset.api.lib.IFluidExtraInformation;
import pl.asie.charset.lib.misc.FluidBase;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;
import scala.xml.dtd.EMPTY;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class FluidPotion extends FluidBase implements IFluidExtraInformation {
	public static final ResourceLocation TEXTURE_STILL = new ResourceLocation("charset:blocks/dyed_water_still");
	public static final ResourceLocation TEXTURE_FLOWING = new ResourceLocation("charset:blocks/dyed_water_flow");

	private final String unlName;

	public FluidPotion(String fluidName, String unlName) {
		super(fluidName, TEXTURE_STILL, TEXTURE_FLOWING);
		this.unlName = unlName;
	}

	@Override
	public String getUnlocalizedName() {
		return unlName;
	}

	public static List<PotionEffect> getEffectsFromStack(FluidStack stack) {
		return stack.tag != null ? PotionUtils.getEffectsFromTag(stack.tag) : Collections.emptyList();
	}

	public static PotionType getPotion(FluidStack stack) {
		return PotionUtils.getPotionTypeFromNBT(stack.tag);
	}

	public static void copyFromPotionItem(FluidStack stack, ItemStack itemStack) {
		setPotion(stack, PotionUtils.getPotionTypeFromNBT(itemStack.getTagCompound()));
		if (stack.tag != null && itemStack.hasTagCompound()) {
			if (itemStack.getTagCompound().hasKey("CustomPotionColor", Constants.NBT.TAG_ANY_NUMERIC)) {
				stack.tag.setTag("CustomPotionColor", itemStack.getTagCompound().getTag("CustomPotionColor"));
			}

			if (itemStack.getTagCompound().hasKey("CustomPotionEffects", Constants.NBT.TAG_LIST)) {
				stack.tag.setTag("CustomPotionEffects", itemStack.getTagCompound().getTag("CustomPotionEffects"));
			}
		}
	}

	public static Item getPotionItem(Fluid fluid) {
		if (fluid == CharsetCraftingCauldron.liquidPotion) {
			return Items.POTIONITEM;
		} else if (fluid == CharsetCraftingCauldron.liquidSplashPotion) {
			return Items.SPLASH_POTION;
		} else if (fluid == CharsetCraftingCauldron.liquidLingeringPotion) {
			return Items.LINGERING_POTION;
		} else {
			return Items.POTIONITEM;
		}
	}

	public static void copyToPotionItem(ItemStack itemStack, FluidStack stack) {
		if (stack.tag != null) {
			if (!itemStack.hasTagCompound()) {
				itemStack.setTagCompound(new NBTTagCompound());
			}

			if (stack.tag.hasKey("Potion", Constants.NBT.TAG_STRING)) {
				itemStack.getTagCompound().setTag("Potion", stack.tag.getTag("Potion"));
			}

			if (stack.tag.hasKey("CustomPotionColor", Constants.NBT.TAG_ANY_NUMERIC)) {
				itemStack.getTagCompound().setTag("CustomPotionColor", stack.tag.getTag("CustomPotionColor"));
			}

			if (stack.tag.hasKey("CustomPotionEffects", Constants.NBT.TAG_LIST)) {
				itemStack.getTagCompound().setTag("CustomPotionEffects", stack.tag.getTag("CustomPotionEffects"));
			}
		}
	}

	public static FluidStack setPotion(FluidStack stack, PotionType potionType) {
		ResourceLocation loc = ForgeRegistries.POTION_TYPES.getKey(potionType);

		if (potionType == PotionTypes.EMPTY && stack.tag != null) {
			stack.tag = null;
		} else {
			if (stack.tag == null) {
				stack.tag = new NBTTagCompound();
			}

			stack.tag.setString("Potion", loc.toString());
		}

		return stack;
	}

	@Override
	public int getColor(FluidStack stack) {
		if (stack.tag != null && stack.tag.hasKey("CustomPotionColor", Constants.NBT.TAG_ANY_NUMERIC)) {
			return 0xFF000000 | stack.tag.getInteger("CustomPotionColor");
		}

		return getPotion(stack) == PotionTypes.EMPTY ? 0xFFF800F8 : 0xFF000000 | PotionUtils.getPotionColorFromEffectList(getEffectsFromStack(stack));
	}

	@Override
	public void addInformation(FluidStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		ItemStack itemStack = new ItemStack(Items.POTIONITEM, 1, 0);
		itemStack.setTagCompound(stack.tag);
		PotionUtils.addPotionTooltip(itemStack, tooltip, 1.0F);
	}
}
