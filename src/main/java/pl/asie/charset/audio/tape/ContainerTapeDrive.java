package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.container.ContainerBase;
import pl.asie.charset.lib.container.SlotTyped;

public class ContainerTapeDrive extends ContainerBase {
	public ContainerTapeDrive(IInventory inventory, InventoryPlayer inventoryPlayer) {
		super(inventory, inventoryPlayer);
		this.addSlotToContainer(new SlotTyped(inventory, 0, 80, 34, new Object[]{ModCharsetAudio.tapeItem}));
		this.bindPlayerInventory(inventoryPlayer, 8, 84);
	}
}
