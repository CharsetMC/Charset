package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.util.INBTSerializable;

import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.inventory.InventorySimple;

public class TapeDriveState implements ITickable, INBTSerializable<NBTTagCompound> {
	protected int counter;
	private final PartTapeDrive owner;
	private final InventorySimple inventory;
	private State state = State.STOPPED, lastState;

	public TapeDriveState(PartTapeDrive owner, InventorySimple inventory) {
		this.owner = owner;
		this.inventory = inventory;
	}

	public void setState(State state) {
		this.lastState = this.state;
		this.state = state;
	}

	public int getCounter() {
		return counter;
	}

	public void resetCounter() {
		counter = 0;
	}

	@Override
	public void update() {
		int lastCounter = counter;

		if (state != State.STOPPED) {
			boolean found = false;
			ItemStack stack = inventory.getStackInSlot(0);
			if (stack != null && stack.hasCapability(ModCharsetAudio.CAP_STORAGE, null)) {
				IDataStorage storage = stack.getCapability(ModCharsetAudio.CAP_STORAGE, null);
				if (storage != null) {
					found = true;
					int rel = storage.getPosition();

					if (state == State.PLAYING) {
						byte[] data = new byte[300];
						int len = storage.read(data, false);

						ModCharsetAudio.packet.sendToWatching(new PacketDriveAudio(owner, data), owner);

						if (len < data.length) {
							setState(State.STOPPED);
						}
					} else {
						int offset = 3072 * (state == State.FORWARDING ? 1 : -1);
						int len = storage.seek(offset);
						if (len != offset) {
							setState(State.STOPPED);
						}
					}

					counter += (storage.getPosition() - rel);
				}
			}

			if (!found) {
				setState(State.STOPPED);
			}
		}

		if (lastState != state) {
			ModCharsetAudio.packet.sendToWatching(new PacketDriveState(owner, state), owner);
			if (state == State.STOPPED && lastState == State.PLAYING) {
				ModCharsetAudio.packet.sendToWatching(new PacketDriveStop(owner), owner);
			}
		}

		if (lastCounter != counter) {
			updateCounter();
		}

		lastState = state;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("st", (byte) state.ordinal());
		compound.setInteger("ct", counter);
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		state = State.STOPPED;

		if (nbt != null) {
			if (nbt.hasKey("st")) {
				int stateId = nbt.getByte("st");
				if (stateId >= 0 && stateId < State.values().length) {
					state = State.values()[stateId];
				}
			}

			counter = nbt.getInteger("ct");
		}
	}

	public State getState() {
		return state;
	}

	public IDataStorage getStorage() {
		ItemStack stack = inventory.getStackInSlot(0);
		if (stack != null && stack.hasCapability(ModCharsetAudio.CAP_STORAGE, null)) {
			IDataStorage storage = stack.getCapability(ModCharsetAudio.CAP_STORAGE, null);
			if (storage != null) {
				return storage;
			}
		}

		return null;
	}

	public void updateCounter() {
		PacketDriveCounter packetDriveCounter = new PacketDriveCounter(owner, counter);
		for (EntityPlayer player : inventory.watchers) {
			ModCharsetAudio.packet.sendTo(packetDriveCounter, (EntityPlayerMP) player);
		}
	}
}
