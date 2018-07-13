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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CharsetSounds {
	static final CharsetSounds INSTANCE = new CharsetSounds();

	public static SoundEvent BLOCK_CRANK;

	private SoundEvent register(RegistryEvent.Register<SoundEvent> event, ResourceLocation location) {
		SoundEvent sound = new SoundEvent(location);
		sound.setRegistryName(location);
		event.getRegistry().register(sound);
		return sound;
	}

	@SubscribeEvent
	public void register(RegistryEvent.Register<SoundEvent> event) {
		BLOCK_CRANK = register(event, new ResourceLocation("charset:block.crank"));
	}
}
