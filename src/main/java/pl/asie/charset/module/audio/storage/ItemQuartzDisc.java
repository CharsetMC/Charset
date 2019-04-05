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

package pl.asie.charset.module.audio.storage;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.module.audio.storage.system.DataStorage;

public class ItemQuartzDisc extends Item {
	public static final int DEFAULT_SAMPLE_RATE = 48000;
	private static final int[] SIZES = new int[] {
			DEFAULT_SAMPLE_RATE / 8 * 60,
			DEFAULT_SAMPLE_RATE / 8 * 60 * 3,
			DEFAULT_SAMPLE_RATE / 8 * 60 * 6,
			DEFAULT_SAMPLE_RATE / 8 * 60 * 10
	};
	private static final float[] ARM_STARTS = new float[] {
			23f,
			17f,
			11f,
			5f
	};

	public static class CapabilityProvider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {
		private final ItemStack stack;
		private final IDataStorage dataStorage;

		public CapabilityProvider(ItemStack stack) {
			this.stack = stack;
			this.dataStorage = CharsetAudioStorage.DATA_STORAGE.getDefaultInstance();
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CharsetAudioStorage.DATA_STORAGE;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (dataStorage != null) {
				if (!dataStorage.isInitialized()) {
					dataStorage.initialize(null, 0, CharsetAudioStorage.quartzDisc.getSize(stack));
				}

				if (capability == CharsetAudioStorage.DATA_STORAGE) {
					stack.setItemDamage(stack.getItemDamage() | 1);
					return CharsetAudioStorage.DATA_STORAGE.cast(dataStorage);
				}
			}
			return null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			if (dataStorage != null) {
				NBTTagCompound compound = new NBTTagCompound();
				NBTBase data = CharsetAudioStorage.DATA_STORAGE.writeNBT(dataStorage, null);
				if (data != null) {
					compound.setTag("data", data);
				}
				return compound;
			} else {
				return new NBTTagCompound();
			}
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			if (dataStorage != null && nbt.hasKey("data")) {
				CharsetAudioStorage.DATA_STORAGE.readNBT(dataStorage, null, nbt.getCompoundTag("data"));
			}
		}
	}

	public ItemQuartzDisc() {
		super();
		this.setTranslationKey("charset.quartz_disc");
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	protected float getArmStartPosition(ItemStack stack) {
		return ARM_STARTS[(stack.getMetadata() >> 1) % ARM_STARTS.length];
	}

	int getSize(ItemStack stack) {
		int defSize = SIZES[(stack.getMetadata() >> 1) % SIZES.length];
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("size", Constants.NBT.TAG_ANY_NUMERIC) ? stack.getTagCompound().getInteger("size") : defSize;
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		if ((stack.getItemDamage() & 1) == 0) {
			return "item.charset.quartz_disc.blank";
		} else {
			return "item.charset.quartz_disc";
		}
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (int i = 0; i < SIZES.length; i++) {
				items.add(new ItemStack(this, 1, i << 1));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag advanced) {
		int size = getSize(stack);
		int sizeSec = size / (DEFAULT_SAMPLE_RATE / 8);
		int sizeMin = sizeSec / 60;
		sizeSec %= 60;
		CharsetAudioStorage.addTimeToTooltip(tooltip, sizeMin, sizeSec);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider(stack);
	}
}