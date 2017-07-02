package pl.asie.charset.lib.loader;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.event.FMLEvent;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.ThreeState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;

public class ModuleLoader {
	private static class EnableInformation {
		public boolean isDefault;
		public ThreeState override;
		public boolean dependenciesMet;

		public EnableInformation(boolean isDefault, ThreeState override) {
			this.isDefault = isDefault;
			this.override = override;
			this.dependenciesMet = true; // assume true
		}

		public boolean isEnabled() {
			if (override == ThreeState.YES) {
				return true;
			} else if (override == ThreeState.NO) {
				return false;
			} else {
				return isDefault && dependenciesMet;
			}
		}

		public boolean canBeEnabled() {
			return dependenciesMet;
		}
	}

	public static final ModuleLoader INSTANCE = new ModuleLoader();
	public static final Multimap<Class, String> classNames = HashMultimap.create();

	private static final Multimap<String, String> dependencies = HashMultimap.create();
	private static final Map<Class, List<Pair<String, MethodHandle>>> loaderHandles = new IdentityHashMap<>();

	private static final BiMap<String, Object> loadedModules = HashBiMap.create();
	private static final Map<String, Object> loadedModulesByClass = new HashMap<>();
	private static final Map<String, EnableInformation> enableInfoMap = new HashMap<>();
	private static final Set<Configuration> moduleConfigs = new HashSet<>();

	private static final Joiner joinerComma = Joiner.on(", ");

	private final ClassLoader classLoader = getClass().getClassLoader();

	private ModuleLoader() {

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
			Field f = getClass(data).getDeclaredField(data.getObjectName());
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

	private void addClassNames(ASMDataTable table, Class annotationClass, String confType) {
		for (ASMDataTable.ASMData data : table.getAll(annotationClass.getName())) {
			String id = (String) data.getAnnotationInfo().get("value");
			Property prop = ModCharset.configModules.get(
					"compat",
					confType + ":" + id,
					true
			);
			boolean enabled = prop.getBoolean();

			if (enabled && loadedModules.containsKey(id)) {
				classNames.put(annotationClass, data.getClassName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readDataTable(ASMDataTable table) {
		Multimap<String, String> unmetDependencies = HashMultimap.create();
		Set<String> enabledModules = new HashSet<>();
		Set<String> compatModules = new HashSet<>();
		Map<String, ASMDataTable.ASMData> moduleData = new HashMap<>();

		Property baseProfileProp = ModCharset.configModules.get(
				"general",
				"profile",
				"STABLE"
		);
		baseProfileProp.setComment("Set the base profile for Charset.\nThis will decide whether or not certain modules are accessible.\nAllowed values: STABLE, TESTING, UNSTABLE");

		ModuleProfile profile;
		if (ModCharset.INDEV) {
			profile = ModuleProfile.VERY_UNSTABLE;
		} else if ("STABLE".equals(baseProfileProp.getString().toUpperCase())) {
			profile = ModuleProfile.STABLE;
		} else if ("TESTING".equals(baseProfileProp.getString().toUpperCase())) {
			profile = ModuleProfile.TESTING;
		} else if ("UNSTABLE".equals(baseProfileProp.getString().toUpperCase())) {
			profile = ModuleProfile.UNSTABLE;
		} else {
			throw new RuntimeException("Invalid Charset modules.cfg general.profile setting '" + baseProfileProp.getString() + "'!");
		}

		ConfigCategory category = ModCharset.configModules.getCategory("overrides");
		category.setComment("Overrides can have one of three values: DEFAULT, ENABLE, DISABLE\nDEFAULT will enable the module based on your profile settings and dependency availability.");

		boolean configDirty = false;

		for (ASMDataTable.ASMData data : table.getAll(CharsetModule.class.getName())) {
			Map<String, Object> info = data.getAnnotationInfo();
			String name = (String) info.get("name");
			String desc = (String) info.get("description");
			if (desc == null) desc = "";
			ModuleProfile modProfile = ModuleProfile.valueOf(((ModAnnotation.EnumHolder) info.get("profile")).getValue());
			Boolean isDefault = (Boolean) info.getOrDefault("isDefault", true);
			Boolean compat = modProfile == ModuleProfile.COMPAT;
			Boolean clientOnly = (Boolean) info.getOrDefault("isClientOnly", false);
			Boolean serverOnly = (Boolean) info.getOrDefault("isServerOnly", false);

			moduleData.put(name, data);

			ThreeState override = ThreeState.MAYBE;
			if ((Boolean) info.getOrDefault("isVisible", true)) {
				if (compat) {
					Property prop = ModCharset.configModules.get("compat", name, isDefault);
					if (!prop.getBoolean()) override = ThreeState.NO;
				} else {
					Property prop = ModCharset.configModules.get("overrides", name, "DEFAULT");

					if (desc.length() > 0) desc += " ";
					desc += "[Profile: " + modProfile.name().toUpperCase() + "]";

					if (!desc.equals(prop.getComment())) {
						prop.setComment(desc);
						configDirty = true;
					}

					if ("ENABLE".equals(prop.getString().toUpperCase())) {
						override = ThreeState.YES;
					} else if ("DISABLE".equals(prop.getString().toUpperCase())) {
						override = ThreeState.NO;
					}
				}

			}

			if (clientOnly && !FMLCommonHandler.instance().getSide().isClient()) {
				continue;
			}

			if (serverOnly && !FMLCommonHandler.instance().getSide().isServer()) {
				continue;
			}

			if (!compat && modProfile.ordinal() > profile.ordinal()) {
				continue;
			}

			if (compat) {
				compatModules.add(name);
			}

			EnableInformation enableInfo = new EnableInformation(isDefault, override);
			if (enableInfo.isEnabled()) {
				enabledModules.add(name);
				enableInfoMap.put(name, enableInfo);

				if (!"lib".equals(name)) dependencies.put(name, "lib");
				List<String> deps = (List<String>) info.get("dependencies");
				if (deps != null) {
					dependencies.putAll(name, deps);
				}
			}
		}

		if (ModCharset.configModules.hasChanged() || configDirty) {
			ModCharset.configModules.save();
			configDirty = false;
		}

		int removedCount = 1;
		while (removedCount > 0) {
			removedCount = 0;

			for (String name : enabledModules) {
				boolean canLoad = true;

				if (dependencies.containsKey(name)) {
					for (String dep : dependencies.get(name)) {
						boolean optional = false;
						if (dep.startsWith("optional:")) {
							optional = true;
							dep = dep.substring("optional:".length());
						}

						boolean met = true;
						if (!optional) {
							if (dep.startsWith("mod:")) {
								if (!Loader.isModLoaded(dep.substring("mod:".length()))) {
									met = false;
								}
							} else {
								if (!enabledModules.contains(dep)) {
									met = false;
								}
							}
						}

						if (!met) {
							canLoad = false;
							enableInfoMap.get(name).dependenciesMet = false;
							unmetDependencies.put(name, dep);
						}
					}
				}

				if (canLoad) {
				}
			}

			Iterator<String> unmetDepKey = unmetDependencies.keySet().iterator();
			while (unmetDepKey.hasNext()) {
				String depMod = unmetDepKey.next();
				EnableInformation enableInfo = enableInfoMap.get(depMod);
				if (!enableInfo.isEnabled()) {
					if (!compatModules.contains(depMod)) {
						ModCharset.logger.info("Module " + depMod + " requires " + joinerComma.join(unmetDependencies.get(depMod)) + ", but is not force-enabled. You can ignore this - it is not an error, just information.");
					}

					removedCount++;
					enabledModules.remove(depMod);
					unmetDepKey.remove();
				}
			}
		}

		for (String name : enabledModules) {
			if (ModCharset.INDEV) {
				ModCharset.logger.debug("Instantiating module " + name);
			}
			ASMDataTable.ASMData data = moduleData.get(name);
			try {
				Object o = getClass(data).newInstance();
				loadedModules.put(name, o);
				loadedModulesByClass.put(data.getClassName(), o);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		if (unmetDependencies.size() > 0) {
			List<String> depStrings = new ArrayList<>(unmetDependencies.size());
			for (String depMod : unmetDependencies.keys()) {
				depStrings.add(depMod + "<-[" + joinerComma.join(unmetDependencies.get(depMod)) + "]");
			}
			throw new RuntimeException("The following mandatory dependencies were not met: " + joinerComma.join(depStrings));
		}

		for (Object o : loadedModules.values()) {
			MinecraftForge.EVENT_BUS.register(o);
		}

		iterateModules(table, Mod.EventHandler.class.getName(), (data, instance) -> {
			String methodName = data.getObjectName().substring(0, data.getObjectName().indexOf('('));
			String methodDesc = data.getObjectName().substring(methodName.length());
			MethodType methodType = MethodType.fromMethodDescriptorString(methodDesc, classLoader);
			if (ModCharset.INDEV) {
				if (methodType.parameterCount() != 1) {
					throw new RuntimeException("Invalid parameter count " + methodType.parameterCount() + " for EventHandler in " + instance.getClass() + "!");
				}
			}

			try {
				MethodHandle methodHandle = MethodHandles.lookup().findVirtual(getClass(data), methodName, methodType);
				List<Pair<String, MethodHandle>> list = loaderHandles.computeIfAbsent(methodType.parameterType(0), k -> new ArrayList<>());
				list.sort(Comparator.comparing(Pair::getLeft));

				list.add(Pair.of(loadedModules.inverse().get(instance), methodHandle));
			} catch (NoSuchMethodException e) {
				// method has been annotated away, ignore
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

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
				String channelName = "chrs:" + id.substring(id.lastIndexOf('.') + 1);
				getField(data).set(instance, new PacketRegistry(channelName));
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

		List<String> sortedModules = new ArrayList<>();
		Set<String> remainingModules = Sets.newHashSet(enabledModules);
		while (!remainingModules.isEmpty()) {
			Iterator<String> remainingIterator = remainingModules.iterator();
			boolean added = false;

			while (remainingIterator.hasNext()) {
				String s = remainingIterator.next();
				boolean canAdd = true;

				for (String dep : dependencies.get(s)) {
					if (!dep.startsWith("mod:") && !dep.startsWith("optional:") && !sortedModules.contains(dep)) {
						canAdd = false;
						break;
					}
				}

				if (canAdd) {
					added = true;
					sortedModules.add(s);
					remainingIterator.remove();
				}
			}

			if (!added) {
				throw new RuntimeException("Cyclic dependency within Charset modules! Report!");
			}
		}

		for (List<Pair<String, MethodHandle>> list : loaderHandles.values()) {
			list.sort(Comparator.comparingInt(a -> sortedModules.indexOf(a.getKey())));
		}

		addClassNames(table, CharsetJEIPlugin.class, "jei");
		addClassNames(table, CharsetMCMPAddon.class, "mcmultipart");

		if (ModCharset.configModules.hasChanged() || configDirty) {
			ModCharset.configModules.save();
			configDirty = false;
		}
	}

	public void passEvent(FMLEvent o) {
		Class<? extends FMLEvent> c = o.getClass();
		List<Pair<String, MethodHandle>> list = loaderHandles.get(c);
		if (list != null) {
			for (Pair<String, MethodHandle> pair : list) {
				try {
					pair.getValue().invoke(loadedModules.get(pair.getKey()), o);
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
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
