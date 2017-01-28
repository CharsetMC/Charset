package pl.asie.charset.lib.loader;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.Set;

public class LoaderHandler {
	public static final LoaderHandler INSTANCE = new LoaderHandler();

	private static final Set<MethodHandle> postInitHandles = new HashSet<>();

	private LoaderHandler() {

	}

	public void readDataTable(ASMDataTable table) {
		ClassLoader classLoader = getClass().getClassLoader();

		for (ASMDataTable.ASMData data : table.getAll("pl.asie.charset.lib.loader.ModCompatProvider")) {
			String value = (String) data.getAnnotationInfo().get("value");
			String methodName = data.getObjectName().substring(0, data.getObjectName().indexOf('('));
			String methodDesc = data.getObjectName().substring(methodName.length());

			if (value != null && Loader.isModLoaded(value) && methodDesc.equals("()V")) {
				try {
					Class cls = getClass().getClassLoader().loadClass(data.getClassName());
					postInitHandles.add(MethodHandles.lookup().findStatic(cls, methodName, MethodType.fromMethodDescriptorString("()V", classLoader)));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void postInit() {
		for (MethodHandle handle : postInitHandles) {
			try {
				handle.invokeExact();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
