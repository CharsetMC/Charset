/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.lib.EntityGatherItemsEvent;

public class CharsetLibEventHandler {
    @SubscribeEvent
    public void onGatherItemsEvent(EntityGatherItemsEvent event) {
        Entity entity = event.getEntity();
        if (event.collectsHeld() && entity instanceof EntityLivingBase) {
            event.addStack(((EntityLivingBase) entity).getHeldItemMainhand());
            event.addStack(((EntityLivingBase) entity).getHeldItemOffhand());
        }

        if (event.collectsWorn()) {
            for (ItemStack stack : entity.getArmorInventoryList())
                event.addStack(stack);
        }
    }
}
