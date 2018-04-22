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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.api.lib.IFluidExtraInformation;
import pl.asie.charset.lib.misc.FluidBase;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;

import javax.annotation.Nullable;
import java.util.List;

public class FluidDyedWater extends FluidBase implements IFluidExtraInformation {
	public static final ResourceLocation TEXTURE_STILL = new ResourceLocation("charset:blocks/dyed_water_still");
	public static final ResourceLocation TEXTURE_FLOWING = new ResourceLocation("charset:blocks/dyed_water_flow");

	public FluidDyedWater(String fluidName) {
		super(fluidName, TEXTURE_STILL, TEXTURE_FLOWING);
	}

	@Override
	public String getUnlocalizedName() {
		return "fluid.charset.dyed_water.name";
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

		if (dyes.tagCount() == 1 && ((NBTPrimitive) dyes.get(0)).getByte() == color.getMetadata()) {
			return stack;
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
			NBTTagList dyes = tag.getTagList("dyes", Constants.NBT.TAG_BYTE);
			int c = dyes.tagCount();
			int alpha = CharsetCraftingCauldron.waterAlpha;
			alpha = alpha + ((255 - alpha) * c / 8);
			return (getDyeColor(stack) & 0xFFFFFF) | (alpha << 24);
		}
	}

	private String toString(int c) {
		EnumDyeColor color = EnumDyeColor.byMetadata(c);
		return I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", color));
	}

	@Override
	public void addInformation(FluidStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		int[] dyeCount = new int[EnumDyeColor.values().length];

		NBTTagCompound tag = stack.tag;
		if (tag != null && tag.hasKey("dyes", Constants.NBT.TAG_LIST)) {
			NBTTagList dyes = tag.getTagList("dyes", Constants.NBT.TAG_BYTE);
			int c = dyes.tagCount();
			if (c == 1) {
				tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("tip.charset.dyed_water.pure", toString(((NBTPrimitive) dyes.get(0)).getByte())));
				return;
			}

			for (int i = 0; i < c; i++) {
				int v = ((NBTPrimitive) dyes.get(i)).getByte();
				dyeCount[v]++;
			}

			for (int i = 0; i < dyeCount.length; i++) {
				int v = dyeCount[i];
				if (v > 0) {
					String key = "tip.charset.dyed_water.element." + v;
					if (!I18n.canTranslate(key)) {
						key = "tip.charset.dyed_water.element";
					}

					tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted(key, toString(i), v));
				}
			}
		}
	}
}
