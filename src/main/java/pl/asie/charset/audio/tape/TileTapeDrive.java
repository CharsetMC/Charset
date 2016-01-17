package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.world.IInteractionObject;

import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;

public class TileTapeDrive extends TileEntity implements IInteractionObject, ITickable, IInventory, IInventoryOwner, IAudioSource {
	public State state = State.STOPPED;
	public State lastState = State.STOPPED;

	private InventorySimple inventory = new InventorySimple(1, this) {
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return stack == null || stack.getItem() instanceof ItemTape;
		}
	};

	@Override
	public void update() {
		if (state == State.STOPPED) {
			if (lastState == State.PLAYING) {
				ModCharsetAudio.packet.sendToWatching(new PacketDriveStop(this), this);
			}
		} else {
			boolean found = false;
			ItemStack stack = inventory.getStackInSlot(0);
			System.out.println("0");
			if (stack != null && stack.hasCapability(ModCharsetAudio.CAP_STORAGE, null)) {
				System.out.println("1");
				IDataStorage storage = stack.getCapability(ModCharsetAudio.CAP_STORAGE, null);
				if (storage != null) {
					found = true;

					if (state == State.PLAYING) {
						byte[] data = new byte[205];
						int len = storage.read(data, false);

						ModCharsetAudio.packet.sendToWatching(new PacketDriveAudio(this, data), this);

						if (len < data.length) {
							state = State.STOPPED;
						}
					} else {
						int offset = state == State.FORWARDING ? 2048 : -2048;
						int len = storage.seek(offset);
						if (len != offset) {
							state = State.STOPPED;
						}
					}
				}
			}

			if (!found) {
				state = State.STOPPED;
			}
		}

		if (lastState != state) {
			ModCharsetAudio.packet.sendToWatching(new PacketDriveState(this, state), this);
		}

		lastState = state;
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerTapeDrive(this, playerInventory);
	}

	@Override
	public String getGuiID() {
		return null;
	}

	public void readCustomData(NBTTagCompound nbt) {
		inventory.readFromNBT(nbt, "items");
		if (nbt.hasKey("state")) {
			state = State.values()[nbt.getByte("state")];
		} else {
			state = State.STOPPED;
		}
	}

	public void writeCustomData(NBTTagCompound nbt) {
		inventory.writeToNBT(nbt, "items");
		nbt.setByte("state", (byte) state.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readCustomData(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeCustomData(nbt);
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inventory.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inventory.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inventory.setInventorySlotContents(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		inventory.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		inventory.closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return inventory.isItemValidForSlot(index, stack);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public String getName() {
		return "Tape Drive";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentTranslation("tile.charset.tape_drive.name");
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		markDirty();
	}
}
