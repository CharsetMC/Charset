package pl.asie.charset.audio.tape;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.util.INBTSerializable;

import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;

public class TapeDriveState implements ITickable, INBTSerializable<NBTTagCompound> {
	private final IInventory inventory;
	private State state = State.STOPPED, lastState;

	public TapeDriveState(IInventory inventory) {
		this.inventory = inventory;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	public void update() {
		if (state == State.STOPPED) {
			if (lastState == State.PLAYING) {
				ModCharsetAudio.packet.sendToWatching(new PacketDriveStop((TileEntity) inventory), (TileEntity) inventory);
			}
		} else {
			boolean found = false;
			ItemStack stack = inventory.getStackInSlot(0);
			if (stack != null && stack.hasCapability(ModCharsetAudio.CAP_STORAGE, null)) {
				IDataStorage storage = stack.getCapability(ModCharsetAudio.CAP_STORAGE, null);
				if (storage != null) {
					found = true;

					if (state == State.PLAYING) {
						byte[] data = new byte[300];
						int len = storage.read(data, false);
						System.out.println(storage.getPosition() + " " + data[0] + " " + len);

						ModCharsetAudio.packet.sendToWatching(new PacketDriveAudio((TileEntity) inventory, data), (TileEntity) inventory);

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
			ModCharsetAudio.packet.sendToWatching(new PacketDriveState((TileEntity) inventory, state), (TileEntity) inventory);
		}

		lastState = state;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("st", (byte) state.ordinal());
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
		}
	}

	public State getState() {
		return state;
	}
}
