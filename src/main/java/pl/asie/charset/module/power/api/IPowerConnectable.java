package pl.asie.charset.module.power.api;

import pl.asie.charset.api.energy.EnergyCategory;

public interface IPowerConnectable {
	boolean canProvide(EnergyCategory category);
	boolean canConsume(EnergyCategory category);
}
