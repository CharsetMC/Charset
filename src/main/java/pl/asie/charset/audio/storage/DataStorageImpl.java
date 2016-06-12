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

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.ModCharsetLib;

public class DataStorageImpl implements IDataStorage {
	private String uniqueId;
	private File file;
	private int size;
	private byte[] data;
	private int position;
	private boolean dirty = false;

	public DataStorageImpl() {
	}

	public boolean isInitialized() {
		return data != null;
	}

	public void initialize(String id, int position, int size) {
		if (id == null || id.length() == 0) {
			this.uniqueId = ModCharsetAudio.storage.generateUID();
		} else {
			this.uniqueId = id;
		}

		this.file = ModCharsetAudio.storage.get(this.uniqueId);
		this.position = position;
		this.size = size;
		this.data = new byte[size];

		if (this.position >= size) {
			this.position = size - 1;
		} else if (this.position < 0) {
			this.position = 0;
		}

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

		if (simulate) {
			return (int) data[position] & 0xFF;
		} else {
			return (int) data[position++] & 0xFF;
		}
	}

	public int read(byte[] v, int offset, boolean simulate) {
		int len = Math.min(size - (position + offset) - 1, v.length);

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

		dirty = true;
		data[position++] = v;
	}

	public int write(byte[] v) {
		int len = Math.min(size - (position) - 1, v.length);
		if (len == 0) return 0;

		System.arraycopy(v, 0, data, position, len);
		position += len;

		dirty = true;
		return len;
	}


	public void readFile() throws IOException {
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
			ModCharsetLib.logger.warn("Tape " + getUniqueId() + " might have been corrupted.");
			dirty = true;
		}

		stream.close();
		fileStream.close();
	}

	public void writeFile() throws IOException {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
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
			writeFile();
		}
	}
}
