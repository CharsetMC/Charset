package pl.asie.charset.lib.capability.tools;

import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.tools.IStopwatchTracker;

public class DefaultStopwatchTracker implements IStopwatchTracker {
	@Override
	public AddPositionResult addPosition(String key, BlockPos pos) {
		return AddPositionResult.NONE;
	}

	@Override
	public boolean clearPosition(String key) {
		return false;
	}

	@Override
	public void markChanged(BlockPos pos) {

	}
}
