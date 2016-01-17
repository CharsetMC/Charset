package pl.asie.charset.audio.tape;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.refs.Properties;

/**
 * Created by asie on 12/4/15.
 */
public class BlockTapeDrive extends BlockContainer {
	public static final PropertyBool TAPE_INSERTED = PropertyBool.create("tapeInserted");

	public BlockTapeDrive() {
		super(Material.iron);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		setUnlocalizedName("charset.tapedrive");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileTapeDrive) {
			player.openGui(ModCharsetAudio.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getStateForEntityRender(IBlockState state) {
		return this.getDefaultState().withProperty(Properties.FACING, EnumFacing.NORTH);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.TRANSLUCENT;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(TAPE_INSERTED, true);
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(Properties.FACING, EnumFacing.NORTH);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING).ordinal();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileTapeDrive();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, Properties.FACING, TAPE_INSERTED);
	}
}
