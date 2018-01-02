package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.API;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import pl.asie.charset.api.storage.IBarrel;

/**
 * This mimics the DSU interface used by OpenComputers in 1.7.10.
 */
public class ManagedEnvironmentBarrel extends CharsetManagedEnvironment {
	private final IBarrel barrel;

	public ManagedEnvironmentBarrel(IBarrel barrel) {
		super("barrel", Visibility.Network, 0);
		this.barrel = barrel;
	}

	@Callback(doc = "function():int -- Get the maximum number of stored items.")
	public Object[] getMaxStoredCount(Context context, Arguments args) {
		return new Object[] { barrel.getMaxItemCount() };
	}

	@Callback(doc = "function():int -- Get the number of currently stored items.")
	public Object[] getStoredCount(Context context, Arguments args) {
		return new Object[] { barrel.getItemCount() };
	}

	@Callback(doc = "function():int -- Get the currently stored item type.")
	public Object[] getStoredItemType(Context context, Arguments args) {
		if (API.config.getBoolean("misc.allowItemStackInspection")) {
			return new Object[] { barrel.extractItem(1, true) };
		} else {
			return new Object[] { null, "permission denied" };
		}
	}
}
