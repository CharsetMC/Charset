package mcmultipart.capabilities;

import net.minecraftforge.common.capabilities.Capability;

import java.util.Collection;

// TODO 1.11
public interface ICapabilityWrapper<T> {
	Capability<T> getCapability();
	T wrapImplementations(Collection<T> collection);
}
