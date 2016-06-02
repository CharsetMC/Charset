package pl.asie.charset.lib.capability;

import java.util.Collection;

public interface ICapabilityWrapper<T> {
   T wrapImplementations(Collection<T> collection);
}
