package pl.asie.charset.tweaks;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public abstract class Tweak {
	protected final String configCategory, configKey, configComment;
	protected final boolean isDefault;

	protected boolean enabled;

	public Tweak(String configCategory, String configKey, String configComment, boolean isDefault) {
		this.configCategory = configCategory;
		this.configKey = configKey;
		this.configComment = configComment;
		this.enabled = this.isDefault = isDefault;
	}

	protected void initConfig(Configuration config) {
		ConfigCategory cc = config.getCategory(configCategory);
		if (!cc.containsKey(configKey)) {
			Property prop = new Property(configKey, isDefault ? "true" : "false", Property.Type.BOOLEAN);
			prop.setRequiresMcRestart(!canTogglePostLoad());
			cc.put(configKey, prop);
		}
	}

	public void onConfigChanged(Configuration config, boolean firstLaunch) {
		if (firstLaunch) {
			initConfig(config);
			enabled = config.getBoolean(configKey, configCategory, isDefault, configComment);
		} else if (canTogglePostLoad()) {
			boolean newEnabled = config.getBoolean(configKey, configCategory, isDefault, configComment);
			if (newEnabled == false && enabled == true) {
				enabled = false;
				disable();
			} else if (newEnabled == true && enabled == false) {
				enabled = true;
				enable();
			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean canTogglePostLoad() {
		return true;
	}

	public void preInit() {

	}

	public void init() {

	}

	public void enable() {

	}

	public void disable() {

	}
}
