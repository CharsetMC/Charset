/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.audio.tape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.storage.DataStorageImpl;
import pl.asie.charset.audio.util.TapeUtils;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.items.IDyeableItem;

public class ItemTape extends Item implements IDyeableItem {
	public static final Map<String, Material> materialByName = new HashMap<String, Material>();
	public static final int DEFAULT_SAMPLE_RATE = 48000;
	private static final int DEFAULT_SIZE = 2880000;

	@SideOnly(Side.CLIENT)
	public static class Color implements IItemColor {
		private final ItemTape item;

		public Color(ItemTape item) {
			this.item = item;
		}

		@Override
		public int getColorFromItemstack(ItemStack stack, int renderPass) {
			switch (renderPass) {
				case 0:
				default:
					return 0xFFFFFF;
				case 1:
					Material mat = item.getMaterial(stack);
					return mat != null ? mat.color : Material.IRON.color;
				case 2:
					return item.getColor(stack);
			}
		}
	}

	public enum Material {
		IRON("ingotIron", 0x8C8C8C),
		GOLD("ingotGold", 0xF0E060, TextFormatting.YELLOW + ""),
		DIAMOND("gemDiamond", 0x60E0F0, TextFormatting.AQUA + ""),
		EMERALD("gemEmerald", 0x50E080, TextFormatting.GREEN + ""),
		QUARTZ("gemQuartz", 0xE0E0E0, TextFormatting.WHITE + ""),
		RUBY("gemRuby", 0xE05050),
		SAPPHIRE("gemSapphire", 0x3040E0),
		DARK_IRON("ingotDarkIron", 0x503080, TextFormatting.DARK_PURPLE + "");

		public final String oreDict;
		public final String subtitle;
		public final int color;

		Material(String oreDict, int color) {
			this(oreDict, color, null);
		}

		Material(String oreDict, int color, String subtitle) {
			this.oreDict = oreDict;
			this.color = color;
			this.subtitle = subtitle;

			materialByName.put(name(), this);
		}
	}

	public static class CapabilityProvider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {
		private final ItemStack stack;
		private final IDataStorage dataStorage;

		public CapabilityProvider(ItemStack stack) {
			this.stack = stack;
			this.dataStorage = ModCharsetAudio.CAP_STORAGE.getDefaultInstance();
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == ModCharsetAudio.CAP_STORAGE;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (dataStorage != null) {
				if (!dataStorage.isInitialized()) {
					dataStorage.initialize(null, 0, stack.hasTagCompound() && stack.getTagCompound().hasKey("size") ? stack.getTagCompound().getInteger("size") : DEFAULT_SIZE);
				}

				if (capability == ModCharsetAudio.CAP_STORAGE) {
					if (!((DataStorageImpl) dataStorage).makeReady()) { // HACK
						return null;
					}

					return ModCharsetAudio.CAP_STORAGE.cast(dataStorage);
				}
			}
			return null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			if (dataStorage != null) {
				NBTTagCompound compound = new NBTTagCompound();
				NBTBase data = ModCharsetAudio.CAP_STORAGE.writeNBT(dataStorage, null);
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
				ModCharsetAudio.CAP_STORAGE.readNBT(dataStorage, null, nbt.getCompoundTag("data"));
			}
		}
	}

	public ItemTape() {
		super();
		this.setUnlocalizedName("charset.tape");
		this.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		this.setMaxStackSize(1);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		Material mat = getMaterial(stack);
		return "item.charset.tape" + (mat != null ? "." + mat.name().toLowerCase() : "");
	}

	public static ItemStack asItemStack(int size, Material material) {
		ItemStack stack = new ItemStack(ModCharsetAudio.tapeItem);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("size", size);
		stack.getTagCompound().setString("material", material.name());
		return stack;
	}

	public Material getMaterial(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("material") ? materialByName.get(stack.getTagCompound().getString("material")) : null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (Material mat : Material.values()) {
			if (OreDictionary.doesOreNameExist(mat.oreDict)) {
				subItems.add(asItemStack(480 / 8 * DEFAULT_SAMPLE_RATE, mat));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		Material mat = getMaterial(stack);
		if (mat != null && mat.subtitle != null) {
			tooltip.add(mat.subtitle + I18n.translateToLocal(getUnlocalizedName(stack) + ".tip"));
		}

		int size = stack.hasTagCompound() && stack.getTagCompound().hasKey("size") ? stack.getTagCompound().getInteger("size") : DEFAULT_SIZE;
		int sizeSec = size / (DEFAULT_SAMPLE_RATE / 8);
		int sizeMin = sizeSec / 60;
		sizeSec %= 60;
		TapeUtils.addTooltip(tooltip, sizeMin, sizeSec);
	}

	@Override
	public int getColor(ItemStack stack) {
		if (hasColor(stack)) {
			return stack.getTagCompound().getInteger("labelColor");
		} else {
			return 0xF0F0E8;
		}
	}

	@Override
	public boolean hasColor(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("labelColor");
	}

	@Override
	public void setColor(ItemStack stack, int color) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("labelColor", color);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider(stack);
	}
}
