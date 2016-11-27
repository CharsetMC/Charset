package pl.asie.charset.lib.capability.pipe;

import net.minecraft.item.ItemStack;
import pl.asie.charset.api.pipes.IPipeView;

import java.util.Collection;
import java.util.Collections;

public class DefaultPipeView implements IPipeView {
	@Override
	public Collection<ItemStack> getTravellingStacks() {
		return Collections.EMPTY_SET;
	}
}
