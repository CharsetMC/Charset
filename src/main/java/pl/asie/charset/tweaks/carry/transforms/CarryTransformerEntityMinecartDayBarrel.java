package pl.asie.charset.tweaks.carry.transforms;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.*;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.storage.ModCharsetStorage;
import pl.asie.charset.storage.barrel.EntityMinecartDayBarrel;
import pl.asie.charset.storage.barrel.TileEntityDayBarrel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerEntityMinecartDayBarrel extends CarryTransformerEntityMinecart {
	@Nullable
	@Override
	protected Pair<IBlockState, TileEntity> getExtractedPair(@Nonnull Entity object, boolean simulate) {
		if (object instanceof EntityMinecartDayBarrel) {
			return Pair.of(ModCharsetStorage.barrelBlock.getDefaultState(), ((EntityMinecartDayBarrel) object).getTileInternal());
		} else {
			return null;
		}
	}

	@Override
	public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
		if (state.getBlock() == ModCharsetStorage.barrelBlock) {
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
