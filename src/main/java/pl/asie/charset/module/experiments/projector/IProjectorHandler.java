package pl.asie.charset.module.experiments.projector;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

public interface IProjectorHandler<T> {
	boolean matches(T target);
	float getAspectRatio(T target);
	void render(T target, IProjectorSurface surface);
}
