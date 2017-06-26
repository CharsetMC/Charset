package pl.asie.charset.lib.utils;

import net.minecraft.util.IThreadListener;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class Utils {
	private static final MethodHandle EXPLOSION_SIZE_GETTER;

	static {
		try {
			EXPLOSION_SIZE_GETTER = MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(Explosion.class, "size", "field_77280_f"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Utils() {

	}

	public static IThreadListener getThreadListener() {
		return UtilProxyCommon.proxy;
	}

	public static float getExplosionSize(Explosion explosion) {
		try {
			return (float) EXPLOSION_SIZE_GETTER.invokeExact(explosion);
		} catch (Throwable t) {
			return 1;
		}
	}

	public static World getLocalWorld(int dim) {
		return UtilProxyCommon.proxy.getLocalWorld(dim);
	}


}
