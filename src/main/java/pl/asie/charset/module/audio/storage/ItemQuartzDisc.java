/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.module.audio.storage.system.DataStorage;

public class ItemQuartzDisc extends Item {
	public static final int DEFAULT_SAMPLE_RATE = 48000;
	private static final int DEFAULT_SIZE = 2880000;

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
					dataStorage.initialize(null, 0, stack.hasTagCompound() && stack.getTagCompound().hasKey("size") ? stack.getTagCompound().getInteger("size") : DEFAULT_SIZE);
				}

				if (capability == CharsetAudioStorage.DATA_STORAGE) {
					if (!((DataStorage) dataStorage).initializeContents()) {
						return null;
					}

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
		this.setUnlocalizedName("charset.quartz_disc");
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag advanced) {
		int size = stack.hasTagCompound() && stack.getTagCompound().hasKey("size") ? stack.getTagCompound().getInteger("size") : DEFAULT_SIZE;
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