package pl.asie.charset.module.power.api;

import pl.asie.charset.api.energy.EnergyCategory;

public interface IPowerConnectable {
	Type getConnectionType(EnergyCategory category);

	enum Type {
		NONE,
		PROVIDER,
		TRANSMITTER /* transfers, but does not itself use - can be lossy */,
		CONSUMER,
		INTERACTOR /* provides and consumes */;

		boolean accepts() {
			return this != NONE && this != CONSUMER;
		}

		boolean provides() {
			return this != NONE && this != PROVIDER;
		}
	}
}
