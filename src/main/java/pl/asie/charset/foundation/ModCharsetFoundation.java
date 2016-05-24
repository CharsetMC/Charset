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

package pl.asie.charset.foundation;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetFoundation.MODID, name = ModCharsetFoundation.NAME, version = ModCharsetFoundation.VERSION,
		dependencies = ModCharsetLib.DEP_LIB, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetFoundation {
	public static final String MODID = "CharsetFoundation";
	public static final String NAME = "_";
	public static final String VERSION = "@VERSION@";

	@Mod.Instance(MODID)
	public static ModCharsetFoundation instance;

	public static Logger logger;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetFoundation.MODID);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {

	}
}
