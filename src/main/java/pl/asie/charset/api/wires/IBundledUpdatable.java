package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

public interface IBundledUpdatable {
	/**
	 * This function should be called by an IBundledEmitter when its
	 * output is changed.
	 */
	void onBundledInputChanged(EnumFacing face);
}
