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

package pl.asie.charset.module.audio.storage.compat.opencomputers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.modcompat.opencomputers.TraitOCEnvironment;
import pl.asie.charset.module.audio.storage.TileRecordPlayer;
import pl.asie.charset.module.audio.storage.TraitRecordPlayer;

public class TraitRecordPlayerOC extends TraitOCEnvironment {
	public TraitRecordPlayerOC(TileEntity tile) {
		super(tile, Visibility.Network, "record_player");
	}

	TileRecordPlayer getTile() {
		return (TileRecordPlayer) tile;
	}

	ItemStack getStack() {
		return getTile().getStack();
	}

	IDataStorage getStorage() {
		return getTile().getStorage();
	}

	boolean hasReadyStorage() {
		return getStorage() != null && getStorage().isInitialized();
	}

	int getSampleRate() {
		// TODO
		return 48000;
	}

	int getBytesPerSecond() {
		return getSampleRate() / 8;
	}

	@Override
	public void onConnect(Node node) {

	}

	@Override
	public void onDisconnect(Node node) {

	}

	@Override
	public void onMessage(Message message) {

	}

	@Callback(doc = "function():boolean -- Returns true if there is a record in the player", direct = true)
	public Object[] isReady(Context context, Arguments args) {
		return new Object[] { hasReadyStorage() };
	}

	@Callback(doc = "function():number -- Returns the record player's position, in seconds. Negative values indicate a resting arm.", direct = true)
	public Object[] getPosition(Context context, Arguments args) {
		return new Object[] { hasReadyStorage() ? (double) getStorage().getPosition() / getBytesPerSecond() : -1.0 };
	}

	@Callback(doc = "function():number -- Returns the duration of the record, in seconds.", direct = true)
	public Object[] getDuration(Context context, Arguments args) {
		return new Object[] { hasReadyStorage() ? (double) getStorage().getSize() / getBytesPerSecond() : 0.0 };
	}

	@Callback(doc = "function():number -- Returns the current sampling rate of the record player.", direct = true)
	public Object[] getSampleRate(Context context, Arguments args) {
		return new Object[] { getSampleRate() };
	}

	@Callback(doc = "function():string -- Returns the name of the inserted record, if any.", direct = true)
	public Object[] getName(Context context, Arguments args) {
		ItemStack stack = getStack();
		return new Object[] { stack.isEmpty() ? "" : (stack.hasDisplayName() ? stack.getDisplayName() : "") };
	}

	@Callback(doc = "function(duration:number):number -- Seeks the specified amount of seconds on the record. "
			+ "Negative values for rewinding. Returns the number of seconds sought.")
	public Object[] seek(Context context, Arguments args) {
		if (hasReadyStorage()) {
			int seekAmount = (int) Math.round(args.checkDouble(0) * getBytesPerSecond());
			if (seekAmount == 0) {
				return new Object[] { 0.0 };
			} else {
				IDataStorage storage = getStorage();
				int oldPos = storage.getPosition();
				int newPos = oldPos + seekAmount;
				if (newPos < 0) newPos = 0;
				else if (newPos > storage.getSize()) newPos = storage.getSize();
				storage.setPosition(newPos);
				return new Object[] { (double) (newPos - oldPos) / getBytesPerSecond() };
			}
		} else {
			return new Object[] { 0.0 };
		}
	}

	@Callback(doc = "function():string -- Returns the current state of the player: \"playing\", \"paused\", \"stopped\" or \"recording\".", direct = true)
	public Object[] getState(Context context, Arguments args) {
		return new Object[] { getTile().getState().name().toLowerCase() };
	}

	@Callback(doc = "function():boolean -- Sets the record player to play. Returns true if successful.")
	public Object[] play(Context context, Arguments args) {
		if (hasReadyStorage()) {
			getTile().setState(TraitRecordPlayer.State.PLAYING);
			return new Object[] { getTile().getState() == TraitRecordPlayer.State.PLAYING };
		} else {
			return new Object[] { false };
		}
	}

	@Callback(doc = "function():boolean -- Sets the record player to pause. Returns true if successful.")
	public Object[] pause(Context context, Arguments args) {
		if (hasReadyStorage()) {
			getTile().setState(TraitRecordPlayer.State.PAUSED);
			return new Object[] { getTile().getState() == TraitRecordPlayer.State.PAUSED };
		} else {
			return new Object[] { false };
		}
	}

	@Callback(doc = "function():boolean -- Sets the record player to record from external devices. Returns true if successful.")
	public Object[] record(Context context, Arguments args) {
		if (hasReadyStorage()) {
			getTile().setState(TraitRecordPlayer.State.RECORDING);
			return new Object[] { getTile().getState() == TraitRecordPlayer.State.RECORDING };
		} else {
			return new Object[] { false };
		}
	}

	@Callback(doc = "function():boolean -- Sets the record player to stop. Returns true if successful.")
	public Object[] stop(Context context, Arguments args) {
		getTile().setState(TraitRecordPlayer.State.STOPPED);
		return new Object[] { getTile().getState() == TraitRecordPlayer.State.STOPPED };
	}
}
