package pl.asie.charset.pipes.shifter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.pipes.pipe.TilePipe;

/**
 * Created by asie on 1/28/17.
 */
public class ShifterExtractionHandlerItems implements TileShifter.ExtractionHandler<IItemHandler> {
	@Override
	public Capability<IItemHandler> getCapability() {
		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public TileShifter.ExtractionType getExtractionType() {
		return TileShifter.ExtractionType.ITEMS;
	}

	@Override
	public EnumActionResult extract(IItemHandler handler, TilePipe output, TileShifter shifter, EnumFacing direction) {
		if (output.isLikelyToFailInsertingItem(direction))
			return EnumActionResult.FAIL;

		IItemInsertionHandler outHandler = CapabilityHelper.get(Capabilities.ITEM_INSERTION_HANDLER, output, direction.getOpposite());
		if (outHandler != null) {
			for (int i = 0; i < handler.getSlots(); i++) {
				ItemStack source = handler.getStackInSlot(i);
				if (!source.isEmpty() && shifter.matches(source)) {
					int maxSize = 1;
					ItemStack stack = handler.extractItem(i, maxSize, true);
					if (!stack.isEmpty()) {
						if (outHandler.insertItem(stack, true).isEmpty()) {
							stack = handler.extractItem(i, maxSize, false);
							if (!stack.isEmpty()) {
								outHandler.insertItem(stack, false);
							}

							return EnumActionResult.PASS;
						}
					}
				}
			}
		}

		return EnumActionResult.FAIL;
	}
}
