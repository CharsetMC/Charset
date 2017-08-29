package pl.asie.charset.api;

import net.minecraft.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.lib.IBlockCapabilityProvider;
import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;

import javax.annotation.Nullable;

public class CharsetAPI {
	public static CharsetAPI INSTANCE = new CharsetAPI();

	public boolean isPresent() {
		return false;
	}

	public <T> void registerBlockCapabilityProvider(Capability<T> capability, Block block, IBlockCapabilityProvider<T> provider) {
		throw new RuntimeException("Charset API not initialized - please use isPresent()!");
	}

	@Nullable
	public <T> ISimpleInstantiatingRegistry<T> findSimpleInstantiatingRegistry(Class<T> c) {
		throw new RuntimeException("Charset API not initialized - please use isPresent()!");
	}
}
