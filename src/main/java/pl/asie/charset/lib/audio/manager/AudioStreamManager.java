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

package pl.asie.charset.lib.audio.manager;

import net.minecraftforge.fml.common.SidedProxy;

public abstract class AudioStreamManager {
	@SidedProxy(clientSide = "pl.asie.charset.lib.audio.manager.AudioStreamManagerClient", serverSide = "pl.asie.charset.lib.audio.manager.AudioStreamManagerServer")
	public static AudioStreamManager INSTANCE;

	public abstract void put(int id, IAudioStream stream);
	public abstract IAudioStream get(int id);
	public abstract int create();
	public abstract void remove(int id);
	public abstract void removeAll();
}
