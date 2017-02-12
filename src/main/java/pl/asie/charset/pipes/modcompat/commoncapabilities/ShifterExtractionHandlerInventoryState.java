package pl.asie.charset.pipes.modcompat.commoncapabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.pipes.pipe.TilePipe;
import pl.asie.charset.pipes.shifter.TileShifter;

@CharsetModule(
	name = "commoncapabilities:pipes.inventoryState",
	dependencies = {"pipes", "mod:commoncapabilities"},
	isModCompat = true
)
public class ShifterExtractionHandlerInventoryState implements TileShifter.ExtractionHandler<IInventoryState> {
	@CapabilityInject(IInventoryState.class)
	public static Capability<IInventoryState> CAP;

	@Mod.EventHandler
	public void register(FMLInitializationEvent event) {
		TileShifter.registerExtractionHandler(this);
	}

	@Override
	public Capability<IInventoryState> getCapability() {
		return CAP;
	}

	@Override
	public TileShifter.ExtractionType getExtractionType() {
		return TileShifter.ExtractionType.ITEMS;
	}

	@Override
	public int getPriority() {
		return 1000;
	}

	@Override
	public EnumActionResult extract(IInventoryState handler, TilePipe output, TileShifter shifter, EnumFacing direction) {
		int currentHash = handler.getHash();
		if (!shifter.ccInvHashSet || currentHash != shifter.ccInvHash) {
			shifter.ccInvHashSet = true;
			shifter.ccInvHash = currentHash;
			return EnumActionResult.PASS;
		} else {
			return EnumActionResult.FAIL;
		}
	}
}
