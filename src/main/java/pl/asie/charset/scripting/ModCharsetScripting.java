package pl.asie.charset.scripting;


import com.google.common.base.Charsets;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.lib.ModCharsetLib;

import java.io.File;

@Mod(modid = ModCharsetScripting.MODID, name = ModCharsetScripting.NAME, version = ModCharsetScripting.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetScripting {
	public static final String MODID = "charsetscripting";
	public static final String NAME = "$";
	public static final String VERSION = "@VERSION@";

	public static Logger logger;

	/* public void executeScripts() {
		for (File scriptFile : new File("./scripts").listFiles()) {
			if (scriptFile.getName().endsWith(".lua")) {
				try {
					LuaFunction main = loader.loadTextChunk(new Variable(env),
							"scripts:" + scriptFile.getName(), FileUtils.readFileToString(scriptFile, Charsets.UTF_8));
					DirectCallExecutor.newExecutor().call(state, main);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		state = StateContexts.newDefaultInstance();
		loader = CompilerChunkLoader.of("pl.asie.charset.scripting.compiled");
		env = state.newTable();

		BasicLib.installInto(state, env, ScriptRuntimeEnvironment.INSTANCE, null);
		ModuleLib.installInto(state, env, ScriptRuntimeEnvironment.INSTANCE, null, getClass().getClassLoader());
		StringLib.installInto(state, env);
		MathLib.installInto(state, env);
		TableLib.installInto(state, env);
		IoLib.installInto(state, env, ScriptRuntimeEnvironment.INSTANCE);
		Utf8Lib.installInto(state, env);
	} */
}
