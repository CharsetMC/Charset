package pl.asie.charset.audio.tape;

import java.io.File;
import java.util.Random;

import net.minecraftforge.common.DimensionManager;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.utils.MiscUtils;

public class StorageManager {
	private static Random rand = new Random();
	private File saveDir;

	public StorageManager() {
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

	private String generateRandomName() {
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

	public TapeStorage create(int size) {
		String storageName = generateRandomName();
		if (storageName != null) {
			return get(storageName, size, 0);
		} else {
			return null;
		}
	}

	public boolean exists(String name) {
		return new File(saveDir, filename(name)).exists();
	}

	public TapeStorage get(String name, int size, int position) {
		return new TapeStorage(name, new File(saveDir, filename(name)), size, position);
	}
}
