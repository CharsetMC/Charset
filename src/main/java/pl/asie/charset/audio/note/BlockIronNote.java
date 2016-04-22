package pl.asie.charset.audio.note;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

import pl.asie.charset.lib.ModCharsetLib;

public class BlockIronNote extends BlockContainer {
	public BlockIronNote() {
		super(Material.IRON);
		setHardness(1.6F);
		setUnlocalizedName("charset.ironNoteBlock");
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

    /* @Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileIronNote) {
                ((TileIronNote) tile).onRightClick();
            }
        }
        return true;
    } */

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileIronNote();
	}
}
