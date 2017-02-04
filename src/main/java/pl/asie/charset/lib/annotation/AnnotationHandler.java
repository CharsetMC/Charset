package pl.asie.charset.lib.annotation;

import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import pl.asie.charset.lib.ModCharsetBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class AnnotationHandler {
	public static final AnnotationHandler INSTANCE = new AnnotationHandler();
	private static final NonNullList<MethodHandle> postInitHandles = NonNullList.create();
	private static final NonNullList<ModCharsetBase> subMods = NonNullList.create();
	private final ClassLoader classLoader = getClass().getClassLoader();

	private AnnotationHandler() {

	}

	private Class getClass(ASMDataTable.ASMData data) {
		try {
			return getClass().getClassLoader().loadClass(data.getClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Field getStaticField(ASMDataTable.ASMData data) {
		try {
			return getClass(data).getField(data.getObjectName());
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private MethodHandle getStaticMethod(ASMDataTable.ASMData data) {
		String methodName = data.getObjectName().substring(0, data.getObjectName().indexOf('('));
		String methodDesc = data.getObjectName().substring(methodName.length());

		try {
			return MethodHandles.lookup().findStatic(getClass(data), methodName, MethodType.fromMethodDescriptorString(methodDesc, classLoader));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void readDataTable(ASMDataTable table) {
		for (ASMDataTable.ASMData data : table.getAll("pl.asie.charset.lib.annotation.ModCompatProvider")) {
			String value = (String) data.getAnnotationInfo().get("value");

			if (value != null && Loader.isModLoaded(value)) {
				postInitHandles.add(getStaticMethod(data));
			}
		}

		for (Object o : Loader.instance().getReversedModObjectList().keySet()) {
			if (o instanceof ModCharsetBase) {
				subMods.add((ModCharsetBase) o);
			}
		}
	}

	public void preInit(ASMDataTable table) {
		readDataTable(table);

		subMods.forEach(ModCharsetBase::beforePreInit);
	}

	public void init() {

	}

	public void postInit() {
		subMods.forEach(ModCharsetBase::beforePostInit);

		for (MethodHandle handle : postInitHandles) {
			try {
				handle.invokeExact();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
