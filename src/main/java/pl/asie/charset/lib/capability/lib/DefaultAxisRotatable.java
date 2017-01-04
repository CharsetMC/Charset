package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.lib.IAxisRotatable;

public class DefaultAxisRotatable implements IAxisRotatable {
	@Override
	public boolean rotateAround(EnumFacing axis) {
		return false;
	}
}
