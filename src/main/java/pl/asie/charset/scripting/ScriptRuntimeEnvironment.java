package pl.asie.charset.scripting;

import net.minecraft.client.Minecraft;
import net.sandius.rembulan.env.RuntimeEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ScriptRuntimeEnvironment implements RuntimeEnvironment {
	protected static final ScriptRuntimeEnvironment INSTANCE = new ScriptRuntimeEnvironment();
	private final FileSystem fileSystem;
	private final long startMs;

	private ScriptRuntimeEnvironment() {
		startMs = System.currentTimeMillis();
		/* try {
			fileSystem = FileSystems.newFileSystem(new File(".").toPath(), getClass().getClassLoader());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} */
		fileSystem = FileSystems.getDefault();
	}

	@Override
	public InputStream standardInput() {
		return null;
	}

	@Override
	public OutputStream standardOutput() {
		return System.out;
	}

	@Override
	public OutputStream standardError() {
		return System.err;
	}

	@Override
	public FileSystem fileSystem() {
		return fileSystem;
	}

	@Override
	public String getEnv(String name) {
		return null;
	}

	@Override
	public double getCpuTime() {
		return (System.currentTimeMillis() - startMs) / 1000.0;
	}
}
