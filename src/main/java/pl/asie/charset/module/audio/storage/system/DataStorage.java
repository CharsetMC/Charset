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

package pl.asie.charset.module.audio.storage.system;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import pl.asie.charset.ModCharset;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.module.audio.storage.CharsetAudioStorage;

public class DataStorage implements IDataStorage {
	private String uniqueId;
	private File file;
	private int size;
	private byte[] data;
	private int position;
	private boolean dirty;

	public DataStorage() {
	}

	public boolean isInitialized() {
		return data != null;
	}

	boolean initializeContents() {
		if (file == null && CharsetAudioStorage.storageManager.isReady()) {
			if (this.uniqueId == null) {
				this.uniqueId = CharsetAudioStorage.storageManager.generateUID();
			}

			this.file = CharsetAudioStorage.storageManager.getFileForId(this.uniqueId);
			if (!file.exists()) {
				try {
					file.createNewFile();
					writeFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					readFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return file != null;
	}

	public void initialize(String id, int position, int size) {
		if (id == null || id.length() == 0) {
			this.uniqueId = null;
		} else {
			this.uniqueId = id;
		}

		this.position = position;
		this.size = size;
		this.data = new byte[size];

		if (this.position >= size) {
			this.position = size - 1;
		} else if (this.position < 0) {
			this.position = 0;
		}

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public int getPosition() {
		return position;
	}

	public int getSize() {
		return size;
	}

	public int setPosition(int newPosition) {
		if (newPosition < 0) newPosition = 0;
		if (newPosition >= size) newPosition = size - 1;
		this.position = newPosition;
		return newPosition;
	}

	public int trySeek(int dir) {
		int oldPosition = position;
		int newPosition = position + dir;
		if (newPosition < 0) newPosition = 0;
		if (newPosition >= size) newPosition = size - 1;
		return newPosition - oldPosition;
	}

	public int seek(int dir) {
		int seek = trySeek(dir);
		position += seek;
		return seek;
	}

	public int read(boolean simulate) {
		if (position >= size) return 0;

		initializeContents();

		if (simulate) {
			return (int) data[position] & 0xFF;
		} else {
			return (int) data[position++] & 0xFF;
		}
	}

	public int read(byte[] v, int offset, boolean simulate) {
		int len = Math.min(size - (position + offset) - 1, v.length);
		if (len == 0) {
			return 0;
		}

		initializeContents();

		System.arraycopy(data, position + offset, v, 0, len);
		if (!simulate) {
			position += len;
		}

		return len;
	}

	public int read(byte[] v, boolean simulate) {
		return read(v, 0, simulate);
	}

	public void write(byte v) {
		if (position >= size) return;

		initializeContents();

		dirty = true;
		data[position++] = v;
	}

	public int write(byte[] v) {
		int len = Math.min(size - (position) - 1, v.length);
		if (len == 0) {
			return 0;
		}

		initializeContents();

		System.arraycopy(v, 0, data, position, len);
		position += len;

		dirty = true;
		return len;
	}

	void readFile() throws IOException {
		// Before reading the file, save any previous potential copies to
		// prevent race conditions.
		CharsetAudioStorage.storageManager.save();

		FileInputStream fileStream = new FileInputStream(file);
		GZIPInputStream stream = new GZIPInputStream(fileStream);

		int version = stream.read();
		if (version == 1) {
			int b1 = stream.read() & 0xFF;
			int b2 = stream.read() & 0xFF;
			int b3 = stream.read() & 0xFF;
			int b4 = stream.read() & 0xFF;
			this.position = b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
			if (position < 0 || position >= size) {
				position = 0;
			}
		}

		int dataPos = 0;
		try {
			while (dataPos < this.data.length) {
				int s = stream.read(this.data, dataPos, this.data.length - dataPos);
				if (s >= 0) {
					dataPos += s;
				} else {
					break;
				}
			}
		} catch (EOFException e) {
			ModCharset.logger.warn("Audio file " + getUniqueId() + " might have been corrupted.");
			dirty = true;
		}

		stream.close();
		fileStream.close();
	}

	void writeFile() throws IOException {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			initializeContents();

			FileOutputStream fileStream = new FileOutputStream(file);
			GZIPOutputStream stream = new GZIPOutputStream(fileStream);

			stream.write(2);
			stream.write(data);
			stream.finish();
			stream.flush();
			stream.close();
			fileStream.close();

			dirty = false;
		}
	}

	public void onUnload() throws IOException {
		if (dirty) {
			CharsetAudioStorage.storageManager.markSaveNeeded(this);
		}
	}
}
