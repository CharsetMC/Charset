package pl.asie.charset.lib.stagingapi;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IConfigurationHolder {
	enum DeserializationResult {
		CHANGED_ACCURATE,
		CHANGED_INACCURATE,
		UNCHANGED,
		INVALID
	}

	ResourceLocation getConfigType();
	default boolean acceptsConfigType(ResourceLocation location) {
		return location.equals(getConfigType());
	}
	NBTTagCompound serializeConfig();
	DeserializationResult deserializeConfig(NBTTagCompound compound, ResourceLocation type);
}
