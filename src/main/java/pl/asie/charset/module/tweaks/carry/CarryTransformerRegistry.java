package pl.asie.charset.module.tweaks.carry;

import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CarryTransformerRegistry {
	public static final CarryTransformerRegistry INSTANCE = new CarryTransformerRegistry();
	private final Set<ICarryTransformer<Entity>> entityTransformers;
	private final Set<ICarryTransformer<Entity>> entityTransformersView;

	public CarryTransformerRegistry() {
		entityTransformers = new HashSet<>();
		entityTransformersView = Collections.unmodifiableSet(entityTransformers);
	}

	public void registerEntityTransformer(ICarryTransformer<Entity> transformer) {
		entityTransformers.add(transformer);
	}

	public Set<ICarryTransformer<Entity>> getEntityTransformers() {
		return entityTransformersView;
	}
}
