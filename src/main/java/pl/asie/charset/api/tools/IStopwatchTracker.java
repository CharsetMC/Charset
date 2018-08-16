package pl.asie.charset.api.tools;

import net.minecraft.util.math.BlockPos;

public interface IStopwatchTracker {
	AddPositionResult addPosition(String key, BlockPos pos);
	boolean clearPosition(String key);
	void markChanged(BlockPos pos);

	enum AddPositionResult {
		NONE,
		START,
		END
	}
}
