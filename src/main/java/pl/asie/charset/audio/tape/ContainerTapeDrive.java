/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.container.ContainerBase;
import pl.asie.charset.lib.container.IContainerHandler;
import pl.asie.charset.lib.container.SlotTyped;

public class ContainerTapeDrive extends ContainerBase {
	public ContainerTapeDrive(IItemHandler handler, IContainerHandler containerHandler, InventoryPlayer inventoryPlayer) {
		super(inventoryPlayer, containerHandler);
		this.addSlotToContainer(new SlotTyped(handler, 0, 80, 34, new Object[]{ModCharsetAudio.tapeItem}));
		this.bindPlayerInventory(inventoryPlayer, 8, 84);
	}
}
