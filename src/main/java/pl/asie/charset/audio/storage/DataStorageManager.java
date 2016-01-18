package pl.asie.charset.audio.storage;

import java.io.File;
import java.util.Random;

import net.minecraftforge.common.DimensionManager;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.utils.MiscUtils;

public class DataStorageManager {
	private static final Random rand = new Random();
	private File saveDir;

	public DataStorageManager() {
		File saveDirParent = new File(DimensionManager.getCurrentSaveRootDirectory(), "charset");
		if (saveDirParent.exists() || saveDirParent.mkdir()) {
			saveDir = new File(saveDirParent, "tape");
			if (!saveDir.exists() && !saveDir.mkdir()) {
				ModCharsetAudio.logger.error("Could not create save directory! " + saveDirParent.getAbsolutePath());
			}
		} else {
			ModCharsetAudio.logger.error("Could not create save directory! " + saveDirParent.getAbsolutePath());
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
				String name = MiscUtils.asHexString(nameHex);
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

	public File get(String name) {
		return new File(saveDir, filename(name));
	}
}
