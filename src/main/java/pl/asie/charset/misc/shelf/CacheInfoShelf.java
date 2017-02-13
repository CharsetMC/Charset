package pl.asie.charset.misc.shelf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.RenderUtils;

public class CacheInfoShelf implements IRenderComparable<CacheInfoShelf> {
	public final TextureAtlasSprite plank;
	public final EnumFacing facing;
	public final boolean back;

	private CacheInfoShelf(TextureAtlasSprite plank, EnumFacing facing, boolean back) {
		this.plank = plank;
		this.facing = facing;
		this.back = back;
	}

	public static CacheInfoShelf from(IBlockState state, TileShelf tile) {
		return new CacheInfoShelf(RenderUtils.getItemSprite(tile.getPlank().getStack()), state.getValue(Properties.FACING4), state.getValue(BlockShelf.BACK));
	}

	public static CacheInfoShelf from(ItemStack stack) {
		TileShelf tile = new TileShelf();
		tile.loadFromStack(stack);
		return new CacheInfoShelf(RenderUtils.getItemSprite(tile.getPlank().getStack()), EnumFacing.SOUTH, true);
	}

	@Override
	public boolean renderEquals(CacheInfoShelf other) {
		return other.plank == plank && other.facing == facing && other.back == back;
	}

	@Override
	public int renderHashCode() {
		return (back ? 31 : 0) + plank.hashCode() * 5 + facing.ordinal();
	}
}
