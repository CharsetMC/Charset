package pl.asie.charset.lib.capability.staging;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.stagingapi.IConfigurationHolder;

public class DefaultConfigurationHolder implements IConfigurationHolder {
	@Override
	public ResourceLocation getConfigType() {
		return new ResourceLocation("charset:dummy");
	}

	@Override
	public boolean acceptsConfigType(ResourceLocation location) {
		return false;
	}

	@Override
	public NBTTagCompound serializeConfig() {
		return new NBTTagCompound();
	}

	@Override
	public DeserializationResult deserializeConfig(NBTTagCompound compound, ResourceLocation type) {
		return DeserializationResult.INVALID;
	}
}
