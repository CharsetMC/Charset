package pl.asie.charset.lib.config;

import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public interface ICharsetModuleConfigGui {
	List<IConfigElement> createConfigElements();
}
