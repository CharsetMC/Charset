package pl.asie.charset.module.experiments.projector;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IProjectorSurface {
	World getWorld();
	Vec3d getCornerStart();
	Vec3d getCornerEnd();
	EnumFacing getScreenFacing();
	int getRotation();
	float getWidth();
	float getHeight();

	float[] createUvArray(int uStart, int uEnd, int vStart, int vEnd);

	void restoreGLColor();
}
