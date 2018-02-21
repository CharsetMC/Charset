package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.lib.utils.SpaceUtils;

import javax.annotation.Nullable;

public class BlockSocket extends BlockBase implements ITileEntityProvider {
	private static final AxisAlignedBB[] BOXES_BASE = new AxisAlignedBB[6];

	static {
		BOXES_BASE[0] = new AxisAlignedBB(0,0.75f,0,1,1.0f,1);
		for (int i = 1; i < 6; i++) {
			BOXES_BASE[i] = RotationUtils.rotateFace(BOXES_BASE[0], EnumFacing.getFront(i));
		}
	}

	public BlockSocket() {
		super(Material.WOOD);
		setOpaqueCube(false);
		setFullCube(false);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOXES_BASE[state.getValue(Properties.FACING).ordinal()];
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(Properties.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public final boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == base_state.getValue(Properties.FACING).getOpposite();
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		if (face.getAxis() == state.getValue(Properties.FACING).getAxis()) {
			return BlockFaceShape.SOLID;
		} else {
			return BlockFaceShape.UNDEFINED;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING, EnumFacing.getFront(meta));
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileSocket();
	}
}
