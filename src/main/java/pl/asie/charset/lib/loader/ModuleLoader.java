/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.modcompat.chiselsandbits.CharsetChiselsAndBitsPlugin;
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
	public static final BiMap<String, Configuration> moduleConfigs = HashBiMap.create();
	public static final Map<String, String> moduleGuiClasses = new HashMap<>();

	private static final Multimap<String, String> dependencies = HashMultimap.create();
	private static final Map<Class, List<Pair<String, MethodHandle>>> loaderHandles = new IdentityHashMap<>();

	private static final BiMap<String, Object> loadedModules = HashBiMap.create();
	private static final Map<String, Object> loadedModulesByClass = new HashMap<>();
	private static final Map<String, EnableInformation> enableInfoMap = new HashMap<>();

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

	private boolean isDepPresent(String dep, Collection<String> enabledModules) {
		if (dep.startsWith("mod:")) {
			return Loader.isModLoaded(dep.substring("mod:".length()));
		} else {
			return enabledModules.contains(dep);
		}
	}

	private ModuleProfile getProfileFromString(String s) {
		s = s.toUpperCase();
		if ("STABLE".equals(s)) {
			return ModuleProfile.STABLE;
		} else if ("TESTING".equals(s)) {
			return ModuleProfile.TESTING;
		} else if ("EXPERIMENTAL".equals(s)
				|| "UNSTABLE".equals(s)) {
			return ModuleProfile.EXPERIMENTAL;
		} else {
			throw new RuntimeException("Invalid Charset modules.cfg general.profile setting '" + s + "'!");
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
				"DEFAULT"
		);
		baseProfileProp.setValidValues(new String[] {
				"DEFAULT", "STABLE", "TESTING", "EXPERIMENTAL"
		});
		baseProfileProp.setLanguageKey("config.charset.profile.name");
		baseProfileProp.setRequiresMcRestart(true);

		ModuleProfile profile, defaultProfile;
		if (ModCharset.INDEV) {
			defaultProfile = ModuleProfile.INDEV;
		} else if (ModCharset.defaultOptions.containsKey("profile")) {
			defaultProfile = getProfileFromString(ModCharset.defaultOptions.get("profile"));
		} else {
			defaultProfile = ModuleProfile.STABLE;
		}

		baseProfileProp.setComment("Set the base profile for Charset.\nThis will decide whether or not certain modules are accessible.\nAllowed values: STABLE, TESTING, EXPERIMENTAL\nDEFAULT is " + defaultProfile.name());

		if ("DEFAULT".equals(baseProfileProp.getString().toUpperCase())) {
			profile = defaultProfile;
		} else {
			profile = getProfileFromString(baseProfileProp.getString());
		}
		ModCharset.profile = profile;
		ModCharset.logger.info("Charset profile is " + ModCharset.profile);

		ConfigCategory category = ModCharset.configModules.getCategory("overrides");
		category.setComment("Overrides can have one of three values: DEFAULT, ENABLE, DISABLE\nDEFAULT will enable the module based on your profile settings and dependency availability.");

		category = ModCharset.configModules.getCategory("categories");
		category.setComment("This section allows you to disable certain categories of content, based on a tag system.");

		boolean configDirty = false;

		Map<String, Boolean> categoryMap = new HashMap<>();

		// Initialize categories
		for (ASMDataTable.ASMData data : table.getAll(CharsetModule.class.getName())) {
			Map<String, Object> info = data.getAnnotationInfo();
			List<String> tags = (List<String>) info.getOrDefault("categories", Collections.emptyList());
			for (String s : tags) {
				if (!categoryMap.containsKey(s)) {
					Property prop = ModCharset.configModules.get("categories", s, !"overhaul".equals(s));
					prop.setRequiresMcRestart(true);
					categoryMap.put(s, prop.getBoolean());
				}
			}
		}

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
			List<String> tags = (List<String>) info.getOrDefault("categories", Collections.emptyList());

			String moduleGuiClass = (String) info.getOrDefault("moduleConfigGui", "");
			if (moduleGuiClass.length() > 0) {
				moduleGuiClasses.put(name, moduleGuiClass);
			}

			moduleData.put(name, data);

			ThreeState override = ThreeState.MAYBE;
			if ((Boolean) info.getOrDefault("isVisible", true)) {
				if (modProfile == ModuleProfile.INDEV && profile != ModuleProfile.INDEV) {
					override = ThreeState.NO;
				} else {
					if (compat) {
						Property prop = ModCharset.configModules.get("compat", name, isDefault);
						prop.setRequiresMcRestart(true);
						if (!prop.getBoolean()) override = ThreeState.NO;
					} else {
						Property prop = ModCharset.configModules.get("overrides", name, "DEFAULT");
						prop.setValidValues(new String[] {
								"DEFAULT", "ENABLE", "DISABLE"
						});
						prop.setRequiresMcRestart(true);

						if (desc.length() > 0) desc += " ";
						desc += "[Profile: " + modProfile.name().toUpperCase() + "";
						if (!isDefault) {
							desc += ", off by default!";
						}
						desc += "]";

						if (!desc.equals(prop.getComment())) {
							prop.setComment(desc);
							configDirty = true;
						}

						if (prop.getString().toUpperCase().startsWith("ENABLE")) {
							override = ThreeState.YES;
						} else if (prop.getString().toUpperCase().startsWith("DISABLE")) {
							override = ThreeState.NO;
						} else if (!"DEFAULT".equals(prop.getString().toUpperCase())) {
							ModCharset.logger.warn("Invalid value for '" + name + "' override: '" + prop.getString() + ";");
						}
					}
				}
			}

			if (clientOnly && !FMLCommonHandler.instance().getSide().isClient()) {
				continue;
			}

			if (serverOnly && !FMLCommonHandler.instance().getSide().isServer()) {
				continue;
			}

			if (compat) {
				compatModules.add(name);
			}

			if (override == ThreeState.MAYBE && isDefault) {
				List<String> antideps = (List<String>) info.get("antidependencies");
				if (antideps != null) {
					for (String dep : antideps) {
						if (isDepPresent(dep, enabledModules)) {
							ModCharset.logger.info("Antidependency " + dep + " is present - disabling otherwise not forced module " + name + ".");
							isDefault = false;
							break;
						}
					}
				}

				for (String s : tags) {
					if (!categoryMap.get(s)) {
						ModCharset.logger.info("Category " + s + " is disabled - disabling otherwise not forced module " + name + ".");
						isDefault = false;
					}
				}
			}

			if (!compat && modProfile.ordinal() > profile.ordinal()) {
				isDefault = false;
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
				if (dependencies.containsKey(name)) {
					for (String dep : dependencies.get(name)) {
						boolean optional = false;
						if (dep.startsWith("optional:")) {
							optional = true;
							dep = dep.substring("optional:".length());
						}

						boolean met = optional || isDepPresent(dep, enabledModules);

						if (!met) {
							enableInfoMap.get(name).dependenciesMet = false;
							unmetDependencies.put(name, dep);
							break;
						}
					}
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
				ModCharset.logger.info("Instantiating module " + name);
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
				moduleConfigs.put(id, config);
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

		for (String s : sortedModules) {
			MinecraftForge.EVENT_BUS.register(loadedModules.get(s));
		}


		for (ASMDataTable.ASMData data : table.getAll(CharsetCompatAnnotation.class.getName())) {
			String id = (String) data.getAnnotationInfo().get("value");
			try {
				addClassNames(table, Class.forName(data.getClassName()), id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		passEvent(new CharsetLoadConfigEvent(true));

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
					t.printStackTrace();
					throw new RuntimeException(t);
				}
			}
		}
	}

	public void preInit(ASMDataTable table) {
		readDataTable(table);
	}

	public void init() {
		for (Configuration c : moduleConfigs.values()) {
			if (c.hasChanged()) {
				c.save();
			}
		}
	}

	public void postInit() {
	}
}
