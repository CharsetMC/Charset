package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

public interface IRedstoneUpdatable {
	/**
	 * This function should be called by an IRedstoneEmitter when its
	 * output is changed.
	 */
	void onRedstoneInputChanged(EnumFacing face);
}
