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

package pl.asie.charset.api.tape;

import java.io.IOException;

/**
 * The data storage class used, for example, by CharsetAudio.
 */
public interface IDataStorage {
	void initialize(String id, int position, int size);
	boolean isInitialized();

	String getUniqueId();
	int getPosition();
	int getSize();

	int setPosition(int position);
	int seek(int offset);

	int read(boolean simulate);
	int read(byte[] v, boolean simulate);
	int read(byte[] v, int offset, boolean simulate);

	void write(byte v);
	int write(byte[] v);

	void onUnload() throws IOException;
}
