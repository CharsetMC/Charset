/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.audio.storage.compat.opencomputers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.module.audio.storage.TileRecordPlayer;

@CharsetModule(
        name = "opencomputers:audio.storage",
        profile = ModuleProfile.COMPAT,
        dependencies = {"audio.storage", "mod:opencomputers"}
)
public class CharsetAudioStorageCompatOC {
    @SubscribeEvent
    public void onAttach(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileRecordPlayer) {
            ((TileRecordPlayer) event.getObject()).registerTrait("opencomputers", new TraitRecordPlayerOC(event.getObject()));
        }
    }
}
