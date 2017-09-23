package pl.asie.charset.lib.config;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import pl.asie.charset.lib.loader.ModuleLoader;

public final class ConfigUtils {
	private ConfigUtils() {

	}

	private static void prepareCategory(Configuration config, String category) {
		ConfigCategory configCategory = config.getCategory(category);
		configCategory.setLanguageKey("config.charset." + ModuleLoader.moduleConfigs.inverse().get(config) + "." + category + ".name");
	}

	public static boolean getBoolean(Configuration config, String category, String name, boolean defaultValue, String comment, boolean requiresRestart) {
		prepareCategory(config, category);
		Property prop = config.get(category, name, defaultValue);
		prop.setComment(comment);
		prop.setRequiresMcRestart(requiresRestart);
		prop.setLanguageKey("config.charset." + ModuleLoader.moduleConfigs.inverse().get(config) + "." + category + "." + name + ".name");
		return prop.getBoolean();
	}

	public static String[] getStringList(Configuration config, String category, String name, String[] defaultValue, String comment, boolean requiresRestart) {
		prepareCategory(config, category);
		Property prop = config.get(category, name, defaultValue);
		prop.setComment(comment);
		prop.setRequiresMcRestart(requiresRestart);
		prop.setLanguageKey("config.charset." + ModuleLoader.moduleConfigs.inverse().get(config) + "." + name + ".name");
		return prop.getStringList();
	}

	public static int getInt(Configuration config, String category, String name, int defaultValue, int minValue, int maxValue, String comment, boolean requiresRestart) {
		prepareCategory(config, category);
		Property prop = config.get(category, name, defaultValue);
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		prop.setComment(comment);
		prop.setRequiresMcRestart(requiresRestart);
		prop.setLanguageKey("config.charset." + ModuleLoader.moduleConfigs.inverse().get(config) + "." + name + ".name");
		return prop.getInt();
	}

	public static float getFloat(Configuration config, String category, String name, float defaultValue, float minValue, float maxValue, String comment, boolean requiresRestart) {
		return (float) getDouble(config, category, name, defaultValue, minValue, maxValue, comment, requiresRestart);
	}

	public static double getDouble(Configuration config, String category, String name, double defaultValue, double minValue, double maxValue, String comment, boolean requiresRestart) {
		prepareCategory(config, category);
		Property prop = config.get(category, name, defaultValue);
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		prop.setComment(comment);
		prop.setRequiresMcRestart(requiresRestart);
		prop.setLanguageKey("config.charset." + ModuleLoader.moduleConfigs.inverse().get(config) + "." + name + ".name");
		return prop.getDouble();
	}
}
