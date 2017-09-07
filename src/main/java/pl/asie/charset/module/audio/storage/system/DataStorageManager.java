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

package pl.asie.charset.module.audio.storage.system;

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
import pl.asie.charset.ModCharset;

public class DataStorageManager {
	private static final Random rand = new Random();
	private final Map<String, DataStorage> dirtyMap = new HashMap<>();
	private long lastSave = 0L;
	private File saveDir;

	public DataStorageManager() {
		File saveDirParent = new File(DimensionManager.getCurrentSaveRootDirectory(), "charset");
		if (saveDirParent.exists() || saveDirParent.mkdir()) {
			saveDir = new File(saveDirParent, "data_storage");
			if (!saveDir.exists() && !saveDir.mkdir()) {
				ModCharset.logger.error("Could not create save directory! " + saveDirParent.getAbsolutePath());
			}
		} else {
			ModCharset.logger.error("Could not create save directory! " + saveDirParent.getAbsolutePath());
		}

		lastSave = getCurrentTime();
	}

	private long getCurrentTime() {
		return new Date().getTime();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			boolean shouldStore;
			synchronized (dirtyMap) {
				shouldStore = dirtyMap.size() > 0;
			}
			if (shouldStore && (lastSave + 30000L) < getCurrentTime()) {
				try {
					save();
				} catch (IOException e) {

				}
			}
		}
	}

	void markSaveNeeded(DataStorage impl) {
		if (impl.getUniqueId() != null) {
			synchronized (dirtyMap) {
				dirtyMap.put(impl.getUniqueId(), impl);
			}
		}
	}

	public void save() throws IOException {
		Set<DataStorage> dirtySetClone = new HashSet<>();
		synchronized (dirtyMap) {
			dirtySetClone.addAll(dirtyMap.values());
			dirtyMap.clear();
		}

		for (DataStorage impl : dirtySetClone) {
			impl.writeFile();
		}
	}

	private String filename(String storageName) {
		return storageName + ".dat";
	}

	String generateUID() {
		for (int j = 16; j <= 32; j++) {
			for (int i = 0; i < 256; i++) {
				byte[] nameHex = new byte[j];
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
		return getFileForId(name).exists();
	}

	public boolean isReady() {
		return saveDir != null;
	}

	public File getFileForId(String name) {
		return new File(saveDir, filename(name));
	}
}
