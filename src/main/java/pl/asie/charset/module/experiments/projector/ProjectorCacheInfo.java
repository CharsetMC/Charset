package pl.asie.charset.module.experiments.projector;

import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.Orientation;

public class ProjectorCacheInfo implements IRenderComparable<ProjectorCacheInfo> {
	final Orientation orientation;

	public ProjectorCacheInfo(Orientation o) {
		this.orientation = o;
	}

	public static ProjectorCacheInfo from(TileProjector projector) {
		return new ProjectorCacheInfo(projector.getOrientation());
	}

	public static ProjectorCacheInfo from(ItemStack is) {
		return new ProjectorCacheInfo(Orientation.FACE_NORTH_POINT_UP);
	}

	@Override
	public boolean renderEquals(ProjectorCacheInfo other) {
		return other.orientation == orientation;
	}

	@Override
	public int renderHashCode() {
		return orientation.hashCode();
	}
}
