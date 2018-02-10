package pl.asie.charset.lib.utils;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;

public class ReflectionUtils {
	public static Method reflectMethodRecurse(Class<?> c, String deobfName, String obfName, Class... parameterTypes) {
		String nameToFind = ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) ? deobfName : obfName;

		while (c != null) {
			try {
				Method m = c.getDeclaredMethod(nameToFind, parameterTypes);
				m.setAccessible(true);
				return m;
			} catch (NoSuchMethodException e) {
				Class<?> sc = c.getSuperclass();
				c = (sc == c) ? null : sc;
			} catch (Exception e) {
				throw new ReflectionHelper.UnableToFindMethodException(new String[0], e);
			}
		}

		throw new ReflectionHelper.UnableToFindMethodException(new String[0], new Exception());
	}
}
