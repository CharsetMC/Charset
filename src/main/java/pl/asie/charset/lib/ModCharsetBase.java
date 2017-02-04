package pl.asie.charset.lib;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.lib.network.PacketRegistry;

import java.util.Random;

public class ModCharsetBase {
	protected final Random rand = new Random();
	protected PacketRegistry packet;
	protected Logger logger;
	protected Configuration config;
	private final String modid;

	public ModCharsetBase() {
		this.modid = this.getClass().getAnnotation(Mod.class).modid();
	}

	public Random rand() {
		return rand;
	}

	public Logger logger() {
		return logger;
	}

	public Configuration config() {
		return config;
	}

	public PacketRegistry packet() {
		return packet;
	}

	// Called by AnnotationHandler
	public final void beforePreInit() {
		logger = LogManager.getLogger(modid);
		config = new Configuration(ModCharsetLib.instance.getConfigFile(modid + ".cfg"));
		packet = new PacketRegistry(modid);
	}

	// Called by AnnotationHandler
	public final void beforePostInit() {
		if (config.hasChanged()) {
			config.save();
		}
	}
}
