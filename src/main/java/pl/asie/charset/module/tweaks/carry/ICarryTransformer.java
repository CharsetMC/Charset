package pl.asie.charset.module.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICarryTransformer<T> {
	@Nullable Pair<IBlockState, TileEntity> extract(@Nonnull T object, boolean simulate);
	boolean insert(@Nonnull T object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate);
}
