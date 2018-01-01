/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib.audio.manager;

import net.minecraftforge.fml.common.SidedProxy;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;

public abstract class AudioStreamManager {
	@CharsetModule.SidedProxy(clientSide = "pl.asie.charset.lib.audio.manager.AudioStreamManagerClient", serverSide = "pl.asie.charset.lib.audio.manager.AudioStreamManagerServer")
	public static AudioStreamManager INSTANCE;

	public abstract void put(int id, IAudioStream stream);
	public abstract IAudioStream get(int id);
	public abstract int create();
	public abstract void remove(int id);
	public abstract void removeAll();
}
