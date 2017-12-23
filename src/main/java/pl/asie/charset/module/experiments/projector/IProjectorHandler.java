package pl.asie.charset.module.experiments.projector;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IProjectorHandler<T> {
	boolean matches(T target);
	default int getPageCount(T target) {
		return 1;
	}
	float getAspectRatio(T target);

	@SideOnly(Side.CLIENT)
	void render(T target, IProjector projector, IProjectorSurface surface);
}
