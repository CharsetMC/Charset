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

package pl.asie.charset.module.tools.building.chisel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.ui.ContainerBase;
import pl.asie.charset.lib.ui.SlotBlocked;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.module.misc.pocketcraft.CharsetMiscPocketcraft;
import pl.asie.charset.module.misc.pocketcraft.PacketPTAction;

import java.util.ArrayList;
import java.util.List;

public class ContainerChisel extends ContainerBase {
    public final EntityPlayer player;
    public final InventoryPlayer playerInv;
    public final int heldPos;

    public ContainerChisel(EntityPlayer player) {
        super(player.inventory);
        this.player = player;
        this.playerInv = player.inventory;
        heldPos = this.playerInv.currentItem;
    }
}
