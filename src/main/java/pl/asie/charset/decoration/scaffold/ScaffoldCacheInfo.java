package pl.asie.charset.decoration.scaffold;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.RenderUtils;

public class ScaffoldCacheInfo implements IRenderComparable<ScaffoldCacheInfo> {
	public final TextureAtlasSprite plank;

	private ScaffoldCacheInfo(TextureAtlasSprite plank) {
		this.plank = plank;
	}

	public static ScaffoldCacheInfo from(TileScaffold tile) {
		return new ScaffoldCacheInfo(RenderUtils.getItemSprite(tile.getPlank().getStack()));
	}

	public static ScaffoldCacheInfo from(ItemStack stack) {
		TileScaffold tileScaffold = new TileScaffold();
		tileScaffold.loadFromStack(stack);
		return from(tileScaffold);
	}

	@Override
	public boolean renderEquals(ScaffoldCacheInfo other) {
		return other.plank == plank;
	}

	@Override
	public int renderHashCode() {
		return plank.hashCode();
	}
}
