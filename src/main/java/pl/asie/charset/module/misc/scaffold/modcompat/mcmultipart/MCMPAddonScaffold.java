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

package pl.asie.charset.module.misc.scaffold.modcompat.mcmultipart;

import mcmultipart.api.slot.IPartSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;
import pl.asie.charset.module.misc.scaffold.TileScaffold;

@CharsetMCMPAddon("misc.scaffold")
public class MCMPAddonScaffold extends MCMPAddonBase {
    public MCMPAddonScaffold() {
        super(CharsetMiscScaffold.scaffoldBlock, CharsetMiscScaffold.scaffoldItem,
                MultipartScaffold::new, (tile) -> tile instanceof TileScaffold);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterSlots(RegistryEvent.Register<IPartSlot> event) {
        event.getRegistry().register(MultipartScaffold.Slot.INSTANCE);
    }
}
