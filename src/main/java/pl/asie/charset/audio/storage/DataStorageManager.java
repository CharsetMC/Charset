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

package pl.asie.charset.audio.storage;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraftforge.common.DimensionManager;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.binary.Hex;
import pl.asie.charset.audio.ModCharsetAudio;

public class DataStorageManager {
	private static final Random rand = new Random();
	private final Map<String, DataStorageImpl> dirtyMap = new HashMap<>();
	private long lastSave = 0L;
	private File saveDir;

	public DataStorageManager() {
		File saveDirParent = new File(DimensionManager.getCurrentSaveRootDirectory(), "charset");
		if (saveDirParent.exists() || saveDirParent.mkdir()) {
			saveDir = new File(saveDirParent, "tape");
			if (!saveDir.exists() && !saveDir.mkdir()) {
				ModCharsetAudio.instance.logger().error("Could not create save directory! " + saveDirParent.getAbsolutePath());
			}
		} else {
			ModCharsetAudio.instance.logger().error("Could not create save directory! " + saveDirParent.getAbsolutePath());
		}

		lastSave = time();
	}

	private long time() {
		return new Date().getTime();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			boolean shouldStore;
			synchronized (dirtyMap) {
				shouldStore = dirtyMap.size() > 0;
			}
			if (shouldStore && (lastSave + 30000L) < time()) {
				try {
					save();
				} catch (IOException e) {

				}
			}
		}
	}

	void markDirty(DataStorageImpl impl) {
		if (impl.getUniqueId() != null) {
			synchronized (dirtyMap) {
				dirtyMap.put(impl.getUniqueId(), impl);
			}
		}
	}

	public void save() throws IOException {
		Set<DataStorageImpl> dirtySetClone = new HashSet<>();
		synchronized (dirtyMap) {
			dirtySetClone.addAll(dirtyMap.values());
			dirtyMap.clear();
		}

		for (DataStorageImpl impl : dirtySetClone) {
			impl.writeFile();
		}
	}

	private String filename(String storageName) {
		return storageName + ".dsk";
	}

	public String generateUID() {
		int i;
		int j = 16;

		while (j < 32) {
			i = 1000;
			while (i > 0) {
				byte[] nameHex = new byte[16];
				rand.nextBytes(nameHex);
				String name = Hex.encodeHexString(nameHex);
				if (!exists(name)) {
					return name;
				}
			}

			j++;
		}

		return null;
	}

	public boolean exists(String name) {
		return get(name).exists();
	}

	public boolean ready() {
		return saveDir != null;
	}

	public File get(String name) {
		return new File(saveDir, filename(name));
	}
}
