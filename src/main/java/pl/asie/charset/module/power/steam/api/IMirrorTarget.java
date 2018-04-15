package pl.asie.charset.module.power.steam.api;

public interface IMirrorTarget {
	void registerMirror(IMirror mirror);
	void unregisterMirror(IMirror mirror);
}
