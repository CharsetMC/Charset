package pl.asie.charset.module.power.steam;

import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.module.power.steam.api.IMirror;
import pl.asie.charset.module.power.steam.api.IMirrorTarget;

import java.util.HashSet;
import java.util.Set;

public class TileWaterBoiler extends TileBase implements IMirrorTarget {
	private final Set<IMirror> mirrors = new HashSet<>();

	@Override
	public void invalidate(InvalidationType type) {
		super.invalidate(type);
		if (type == InvalidationType.REMOVAL) {
			mirrors.forEach(IMirror::requestMirrorTargetRefresh);
		}
	}

	@Override
	public void registerMirror(IMirror mirror) {
		mirrors.add(mirror);
	}

	@Override
	public void unregisterMirror(IMirror mirror) {
		mirrors.remove(mirror);
	}
}
