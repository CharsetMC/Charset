package pl.asie.charset.audio.tape;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemTape extends Item {
	public static class CapabilityProvider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {
		private IDataStorage dataStorage;

		public CapabilityProvider() {
			dataStorage = ModCharsetAudio.CAP_STORAGE.getDefaultInstance();
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT && capability == ModCharsetAudio.CAP_STORAGE;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				return null;
			}

			if (dataStorage != null && !dataStorage.isInitialized()) {
				dataStorage.initialize(null, 0, 2097152);
			}
			return capability == ModCharsetAudio.CAP_STORAGE ? (T) dataStorage : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			if (dataStorage != null) {
				NBTTagCompound compound = new NBTTagCompound();
				NBTBase data = ModCharsetAudio.CAP_STORAGE.getStorage().writeNBT(
						ModCharsetAudio.CAP_STORAGE, dataStorage, null
				);
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
				ModCharsetAudio.CAP_STORAGE.getStorage().readNBT(
						ModCharsetAudio.CAP_STORAGE, dataStorage, null, nbt.getCompoundTag("data")
				);
			}
		}
	}

	public ItemTape() {
		super();
		this.setUnlocalizedName("charset.tape");
		this.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		switch (renderPass) {
			case 0:
			default:
				return 16777215;
			case 1:
				return 0x848484;
			case 2:
				return 0xC4C4BC;
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider();
	}
}
