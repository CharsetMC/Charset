package pl.asie.charset.module.power.steam.api;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IMirror {
	boolean isMirrorValid();
	Optional<BlockPos> getMirrorTargetPos();
	void requestMirrorTargetRefresh();
}
