package pl.asie.charset.lib.annotation;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLEvent;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.network.PacketRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class AnnotationHandler {
	public static final AnnotationHandler INSTANCE = new AnnotationHandler();
	private static final Multimap<String, String> dependencies = HashMultimap.create();
	private static final TreeMultimap<Class, Pair<String, MethodHandle>> loaderHandles = TreeMultimap.create(
			Comparator.comparing(Class::getName),
			(a, b) -> {
				if (!a.getKey().equals(b.getKey())) {
					boolean forwardDep = dependencies.get(a.getKey()).contains(b.getKey());
					boolean backwardDep = dependencies.get(b.getKey()).contains(a.getKey());
					if (forwardDep && backwardDep) {
						throw new RuntimeException("Circular dependency found! " + a.getKey() + " <-> " + b.getKey());
					} else if (forwardDep) {
						return 1000;
					} else if (backwardDep) {
						return -1000;
					}
				}

				return (int) Math.signum(a.getValue().hashCode() - b.getValue().hashCode());
			}
	);

	private static final BiMap<String, Object> loadedModules = HashBiMap.create();
	private static final Map<String, Object> loadedModulesByClass = new HashMap<>();
	private static final Set<Configuration> moduleConfigs = new HashSet<>();
	private final ClassLoader classLoader = getClass().getClassLoader();

	private AnnotationHandler() {

	}

	public Set<String> getLoadedModules() {
		return loadedModules.keySet();
	}

	private Class getClass(ASMDataTable.ASMData data) {
		try {
			return getClass().getClassLoader().loadClass(data.getClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Field getField(ASMDataTable.ASMData data) {
		try {
			Field f = getClass(data).getField(data.getObjectName());
			f.setAccessible(true);
			return f;
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

	private void iterateModules(ASMDataTable table, String annotation, BiConsumer<ASMDataTable.ASMData, Object> c) {
		for (ASMDataTable.ASMData data : table.getAll(annotation)) {
			if (loadedModulesByClass.containsKey(data.getClassName())) {
				c.accept(data, loadedModulesByClass.get(data.getClassName()));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readDataTable(ASMDataTable table) {
		Set<String> unmetDependencies = new HashSet<>();

		for (ASMDataTable.ASMData data : table.getAll(CharsetModule.class.getName())) {
			Map<String, Object> info = data.getAnnotationInfo();
			String name = (String) info.get("name");
			String desc = (String) info.get("description");
			Boolean enabled = (Boolean) info.getOrDefault("isDefault", true);
			Boolean compat = (Boolean) info.getOrDefault("isModCompat", false);
			if ((Boolean) info.getOrDefault("isVisible", true)) {
				Property prop = ModCharset.configModules.get(
						compat ? "modules.compat" : "modules",
						name,
						enabled
				);
				if (desc != null && desc.length() > 0) prop.setComment(desc);
				enabled = prop.getBoolean();
			}

			if (!"lib".equals(name)) {
				dependencies.put(name, "lib");
			}

			if (enabled) {
				boolean canLoad = true;
				List<String> deps = (List<String>) info.get("dependencies");
				if (deps != null) {
					for (String dep : deps) {
						boolean optional = false;
						if (dep.startsWith("optional:")) {
							optional = true;
							dep = dep.substring("optional:".length());
						}

						dependencies.put(name, dep);

						boolean met = true;
						if (!optional) {
							if (dep.startsWith("mod:")) {
								if (!Loader.isModLoaded(dep.substring("mod:".length()))) {
									met = false;
								}
							} else {
								if (!loadedModules.containsKey(dep)) {
									met = false;
								}
							}
						}

						if (!met) {
							canLoad = false;
							if (!compat) {
								unmetDependencies.add(dep);
							}
						}
					}
				}

				if (canLoad) {
					try {
						Object o = getClass(data).newInstance();
						loadedModules.put(name, o);
						loadedModulesByClass.put(data.getClassName(), o);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (unmetDependencies.size() > 0) {
			String deps = Joiner.on(", ").join(unmetDependencies);
			throw new RuntimeException("The following dependencies were not met: " + deps);
		}

		if (ModCharset.configModules.hasChanged()) {
			ModCharset.configModules.save();
		}

		iterateModules(table, Mod.EventHandler.class.getName(), (data, instance) -> {
			String methodName = data.getObjectName().substring(0, data.getObjectName().indexOf('('));
			String methodDesc = data.getObjectName().substring(methodName.length());
			MethodType methodType = MethodType.fromMethodDescriptorString(methodDesc, classLoader);
			if (methodType.parameterCount() != 1) {
				throw new RuntimeException("Invalid parameter count " + methodType.parameterCount() + " for EventHandler in " + instance.getClass() + "!");
			}

			try {
				MethodHandle methodHandle = MethodHandles.lookup().findVirtual(getClass(data), methodName, methodType);
				loaderHandles.put(methodType.parameterType(0), Pair.of(loadedModules.inverse().get(instance), methodHandle));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		/* for (Class c : loaderHandles.keySet()) {
			System.out.println(c.getName());
			for (Pair<String, MethodHandle> pair : loaderHandles.get(c)) {
				System.out.println("- " + pair.getKey());
			}
		} */

		iterateModules(table, CharsetModule.Instance.class.getName(), (data, instance) -> {
			try {
				String instString = (String) data.getAnnotationInfo().get("value");
				if (instString == null || instString.equals("")) {
					getField(data).set(instance, instance);
				} else {
					Object inst2 = loadedModules.get(instString);
					getField(data).set(instance, inst2);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		iterateModules(table, CharsetModule.PacketRegistry.class.getName(), (data, instance) -> {
			String id = (String) data.getAnnotationInfo().get("value");
			if (id == null) id = loadedModules.inverse().get(instance);
			try {
				getField(data).set(instance, new PacketRegistry("charset:" + id));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		iterateModules(table, CharsetModule.Configuration.class.getName(), (data, instance) -> {
			String id = (String) data.getAnnotationInfo().get("value");
			if (id == null) id = loadedModules.inverse().get(instance);

			try {
				Configuration config = new Configuration(ModCharset.getModuleConfigFile(id));
				getField(data).set(instance, config);
				moduleConfigs.add(config);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void passEvent(FMLEvent o) {
		Class<? extends FMLEvent> c = o.getClass();
		for (Pair<String, MethodHandle> pair : loaderHandles.get(c)) {
			try {
				pair.getValue().invoke(loadedModules.get(pair.getKey()), o);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}

	public void preInit(ASMDataTable table) {
		readDataTable(table);
	}

	public void init() {
		for (Configuration c : moduleConfigs) {
			if (c.hasChanged()) {
				c.save();
			}
		}
	}

	public void postInit() {
	}
}
