package pl.asie.charset.tweaks.carry.transforms;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.storage.barrels.CharsetStorageBarrels;
import pl.asie.charset.storage.barrels.EntityMinecartDayBarrel;
import pl.asie.charset.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerEntityMinecartDayBarrel extends CarryTransformerEntityMinecart {
	@Nullable
	@Override
	protected Pair<IBlockState, TileEntity> getExtractedPair(@Nonnull Entity object, boolean simulate) {
		if (object instanceof EntityMinecartDayBarrel) {
			return Pair.of(CharsetStorageBarrels.barrelBlock.getDefaultState(), ((EntityMinecartDayBarrel) object).getTileInternal());
		} else {
			return null;
		}
	}

	@Override
	public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
		if (state.getBlock() == CharsetStorageBarrels.barrelBlock) {
			Entity out = transform(object, EntityMinecartDayBarrel.class, simulate);
			if (out != null) {
				if (!simulate) {
					((EntityMinecartDayBarrel) out).initFromTile((TileEntityDayBarrel) tile);
				}
				return true;
			}
		}

		return false;
	}
}
