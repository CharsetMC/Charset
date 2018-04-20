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

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;

import javax.annotation.Nullable;

public class FluidDyedWater extends Fluid {
	public static final ResourceLocation TEXTURE_STILL = new ResourceLocation("charset:blocks/dyed_water_still");
	public static final ResourceLocation TEXTURE_FLOWING = new ResourceLocation("charset:blocks/dyed_water_flow");

	public FluidDyedWater(String fluidName) {
		super(fluidName, TEXTURE_STILL, TEXTURE_FLOWING);
	}

	@Override
	public String getUnlocalizedName(FluidStack stack) {
		NBTTagCompound tag = stack.tag;
		if (tag != null && tag.hasKey("dyes", Constants.NBT.TAG_LIST)) {
			NBTTagList dyes = ((NBTTagList) stack.tag.getTag("dyes"));
			if (dyes.tagCount() == 1) {
				return "fluid.charset.dyed_water.pure";
			}
		}

		return "fluid.charset.dyed_water";
	}

	@Nullable
	public FluidStack appendDye(FluidStack stack, EnumDyeColor color) {
		FluidStack newStack;

		if (stack.getFluid() == FluidRegistry.WATER) {
			newStack = new FluidStack(this, stack.amount);
		} else if (stack.getFluid() == this) {
			newStack = stack.copy();
		} else {
			return null;
		}

		if (newStack.tag == null) {
			newStack.tag = new NBTTagCompound();
		}

		NBTTagList dyes = newStack.tag.hasKey("dyes", Constants.NBT.TAG_LIST) ? ((NBTTagList) newStack.tag.getTag("dyes")) : new NBTTagList();
		if (dyes.tagCount() >= 8) {
			return null;
		}

		dyes.appendTag(new NBTTagByte((byte) color.getMetadata()));
		newStack.tag.setTag("dyes", dyes);
		return newStack;
	}

	public int getDyeColor(FluidStack stack) {
		NBTTagCompound tag = stack.tag;
		if (tag == null || !tag.hasKey("dyes", Constants.NBT.TAG_LIST)) {
			return -1;
		} else {
			NBTTagList dyes = ((NBTTagList) stack.tag.getTag("dyes"));
			int[] color = new int[3];
			int scale = 0;
			int count = dyes.tagCount();
			if (dyes.tagCount() == 0) {
				return -1;
			}

			for (int i = 0; i < count; i++) {
				float[] colF = EnumDyeColor.byMetadata(((NBTPrimitive) dyes.get(i)).getByte()).getColorComponentValues();
				int[] col = new int[]{
						(int) (colF[0] * 255.0F),
						(int) (colF[1] * 255.0F),
						(int) (colF[2] * 255.0F)
				};

				scale += Math.max(col[0], Math.max(col[1], col[2]));
				color[0] += col[0];
				color[1] += col[1];
				color[2] += col[2];
			}

			int i1 = color[0] / count;
			int j1 = color[1] / count;
			int k1 = color[2] / count;
			float f3 = (float) scale / (float) count;
			float f4 = (float) Math.max(i1, Math.max(j1, k1));
			i1 = (int) (i1 * f3 / f4);
			j1 = (int) (j1 * f3 / f4);
			k1 = (int) (k1 * f3 / f4);
			return (i1 << 16) + (j1 << 8) + k1;
		}
	}

	@Override
	public int getColor(FluidStack stack) {
		NBTTagCompound tag = stack.tag;
		if (tag == null || !tag.hasKey("dyes", Constants.NBT.TAG_LIST)) {
			return -1;
		} else {
			NBTTagList dyes = tag.getTagList("dyes", Constants.NBT.TAG_ANY_NUMERIC);
			int c = dyes.tagCount();
			int alpha = CharsetCraftingCauldron.waterAlpha;
			alpha = alpha + ((255 - alpha) * c / 8);
			return (getDyeColor(stack) & 0xFFFFFF) | (alpha << 24);
		}
	}
}
