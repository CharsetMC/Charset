package pl.asie.charset.wires.logic;

import net.minecraftforge.fmp.multipart.IMultipartContainer;
import pl.asie.charset.api.wires.IWireInsulated;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.wires.WireUtils;

public class PartWireInsulated extends PartWireNormal implements IWireInsulated {
	@Override
	protected int getRedstoneLevel(IMultipartContainer container, WireFace location) {
		return WireUtils.getInsulatedWireLevel(container, location, type.color());
	}

	@Override
	protected void onSignalChanged(int color) {
		if (getWorld() != null && !getWorld().isRemote) {
			if (color == type.color() || color == -1) {
				propagate(color);
			}
		}
	}

	@Override
	public int getWireColor() {
		return type.color();
	}
}
