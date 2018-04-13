package pl.asie.charset.module.power.electric;

import net.minecraft.util.ResourceLocation;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;

public class WireProviderElectric extends WireProvider {
	@Override
	public Wire create(IWireContainer container, WireFace location) {
		return new WireElectric(container, this, location);
	}

	@Override
	public float getWidth() {
		return 0.5f;
	}

	@Override
	public float getHeight() {
		return 0.25f;
	}

	@Override
	public boolean hasFreestandingWire() {
		return false;
	}

	@Override
	public ResourceLocation getTexturePrefix() {
		return new ResourceLocation("charset:blocks/power_cable/outer");
	}
}
