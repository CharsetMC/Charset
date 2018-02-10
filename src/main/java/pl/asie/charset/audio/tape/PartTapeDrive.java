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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;
import pl.asie.charset.lib.multipart.PartSlab;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.utils.MachineSound;

public class PartTapeDrive extends PartSlab implements IAudioSource, ITickable, IInventoryOwner, ICapabilityProvider {
	public final InventorySimple inventory = new InventorySimple(1, this) {
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return stack == null || stack.getItem() instanceof ItemTape;
		}
	};
	public final InvWrapper invWrapper = new InvWrapper(inventory);

	public ItemStack asItemStack() {
		return new ItemStack(ModCharsetAudio.partTapeDriveItem);
	}

	@Override
	public float getHardness(PartMOP hit) {
		return 3.0F;
	}

	@Override
	public Material getMaterial() {
		return Material.IRON;
	}

	@Override
	public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
		return asItemStack();
	}

	@Override
	public List<ItemStack> getDrops() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(asItemStack());
		if (inventory.getStackInSlot(0) != null) {
			list.add(inventory.getStackInSlot(0));
		}
		return list;
	}

	protected EnumFacing facing = EnumFacing.NORTH;
	protected final TapeDriveState state = new TapeDriveState(this, inventory);

	public State getState() {
		return this.state.getState();
	}

	public void setState(State state) {
		this.state.setState(state);
	}

	@Override
	public ResourceLocation getModelPath() {
		return new ResourceLocation("charsetaudio:tapedrive");
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
		player.openGui(ModCharsetAudio.instance, isTop ? PartSlot.UP.ordinal() : PartSlot.DOWN.ordinal(), getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
		if (!player.worldObj.isRemote) {
			ModCharsetAudio.packet.sendToWatching(new PacketDriveState(this, getState()), this);
		}
		if (player instanceof EntityPlayerMP) {
			ModCharsetAudio.packet.sendTo(new PacketDriveCounter(this, state.counter), (EntityPlayerMP) player);
		}
		return true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		inventory.writeToNBT(nbt, "items");
		nbt.setByte("facing", (byte) facing.ordinal());
		NBTTagCompound stateNbt = state.serializeNBT();
		nbt.setTag("state", stateNbt);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		inventory.readFromNBT(nbt, "items");
		state.deserializeNBT(nbt.getCompoundTag("state"));
		if (nbt.hasKey("facing")) {
			facing = EnumFacing.getFront(nbt.getByte("facing"));
			if (facing == null || facing.getAxis() == EnumFacing.Axis.Y) {
				facing = EnumFacing.NORTH;
			}
		} else {
			facing = EnumFacing.NORTH;
		}
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeByte(facing.ordinal());
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		facing = EnumFacing.getFront(buf.readByte());
	}

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(PartSlab.IS_TOP, isTop()).withProperty(Properties.FACING4, facing);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, PartSlab.IS_TOP, Properties.FACING4);
	}

	@Override
	public void update() {
		if (getWorld() != null) {
			if (!getWorld().isRemote) {
				state.update();
			} else {
				updateSound();
			}
		}
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		markDirty();
	}

	public void writeData(byte[] data, boolean isLast, int totalLength) {
		IDataStorage storage = state.getStorage();
		if (storage != null) {
			setState(State.STOPPED);
			storage.write(data);
			if (isLast) {
				storage.seek(-totalLength);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private MachineSound sound;

	@SideOnly(Side.CLIENT)
	public void updateSound() {
		boolean isLooping = getState() == State.REWINDING || getState() == State.FORWARDING;
		if (isLooping && sound == null) {
			sound = new MachineSound(new ResourceLocation("charsetaudio", "tape_rewind"),
					SoundCategory.BLOCKS,
					getPos().getX() + 0.5F,
					getPos().getY() + (isTop() ? 0.75F : 0.25F),
					getPos().getZ() + 0.5F,
					1.0f, 1.0f);
			Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		} else if (!isLooping && sound != null) {
			sound.endPlaying();
			Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
			sound = null;
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| capability == Capabilities.AUDIO_SOURCE
				|| capability == Capabilities.AUDIO_RECEIVER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(invWrapper);
		}
		if (capability == Capabilities.AUDIO_SOURCE) {
			return Capabilities.AUDIO_SOURCE.cast(this);
		}
		if (capability == Capabilities.AUDIO_RECEIVER) {
			return Capabilities.AUDIO_RECEIVER.cast(state);
		}
		return null;
	}

	protected IDataStorage getStorage() {
		return state.getStorage();
	}

	protected ItemStack getStack() {
		return state.getStack();
	}
}
