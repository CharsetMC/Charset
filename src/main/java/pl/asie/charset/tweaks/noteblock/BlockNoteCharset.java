package pl.asie.charset.tweaks.noteblock;

import net.minecraft.block.BlockNote;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockNoteCharset extends BlockNote {
	public static final PropertyInteger PITCH = PropertyInteger.create("pitch", 0, 24);

	public BlockNoteCharset() {
		super();
		setHardness(0.8F);
		setUnlocalizedName("musicBlock");
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityNote) {
			TileEntityNote note = (TileEntityNote) tileEntity;
			return state.withProperty(PITCH, (int) note.note);
		} else {
			return state;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PITCH);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
}
