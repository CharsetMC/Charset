package pl.asie.charset.api.audio;

import java.io.IOException;

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
