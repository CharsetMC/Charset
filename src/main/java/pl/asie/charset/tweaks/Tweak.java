/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.tweaks;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public abstract class Tweak {
	protected final String configCategory, configKey, configComment;
	protected final boolean isDefault;
	protected final int maxMode;

	protected int mode;

	public Tweak(String configCategory, String configKey, String configComment, boolean isDefault) {
		this(configCategory, configKey, configComment, isDefault, 0);
	}

	public Tweak(String configCategory, String configKey, String configComment, boolean isDefault, int maxMode) {
		this.configCategory = configCategory;
		this.configKey = configKey;
		this.configComment = configComment;
		this.isDefault = isDefault;
		this.maxMode = maxMode;
		this.mode = isDefault ? 1 : 0;
	}

	protected void initConfig(Configuration config) {
		ConfigCategory cc = config.getCategory(configCategory);
		if (!cc.containsKey(configKey)) {
			Property prop;
			if (maxMode <= 0) {
				prop = new Property(configKey, isDefault ? "true" : "false", Property.Type.BOOLEAN);
			} else {
				prop = new Property(configKey, new Integer(maxMode).toString(), Property.Type.INTEGER);
			}
			prop.setRequiresMcRestart(!canTogglePostLoad());
			cc.put(configKey, prop);
		}
	}

	public void onConfigChanged(Configuration config, boolean firstLaunch) {
		if (firstLaunch) {
			initConfig(config);
			mode = maxMode > 0
					? config.getInt(configKey, configCategory, isDefault ? 1 : 0, 0, maxMode, configComment)
					: config.getBoolean(configKey, configCategory, isDefault, configComment) ? 1 : 0;
		} else if (canTogglePostLoad()) {
			int newMode = maxMode > 0
					? config.getInt(configKey, configCategory, isDefault ? 1 : 0, 0, maxMode, configComment)
					: config.getBoolean(configKey, configCategory, isDefault, configComment) ? 1 : 0;
			if (newMode != mode) {
				if (mode > 0) {
					mode = 0;
					disable();
				}
				if (newMode > 0) {
					mode = newMode;
					enable();
				}
			}
		}
	}

	public int getMode() {
		return mode;
	}

	public boolean isEnabled() {
		return mode > 0;
	}

	public boolean canTogglePostLoad() {
		return true;
	}

	public boolean preInit() {
		return true;
	}

	public boolean init() {
		return true;
	}

	public void enable() {

	}

	public void disable() {

	}
}
