package pl.asie.charset.module.power.steam;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class BlockMirror extends BlockBase implements ITileEntityProvider {
	public static final int ROTATIONS = 32;
	public static final PropertyInteger ROT_PROP = PropertyInteger.create("rotation", 0, ROTATIONS); // max = no rotation

	public BlockMirror() {
		super(Material.IRON);
		setHardness(0.5F); // TODO
		setOpaqueCube(false);
		setFullCube(false);
		setTickRandomly(true);
		setUnlocalizedName("charset.solar_mirror");
		setDefaultState(getDefaultState().withProperty(ROT_PROP, ROTATIONS));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileMirror) {
			Optional<BlockPos> targetPos = ((TileMirror) tile).getMirrorTargetPos();
			if (targetPos.isPresent()) {
				return state.withProperty(ROT_PROP,
						(int) ((MathHelper.atan2(
								targetPos.get().getX() - pos.getX(),
								targetPos.get().getZ() - pos.getZ()
						) + Math.PI) * ROTATIONS / Math.PI / 2) % ROTATIONS
				);
			}
		}

		return state;
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileMirror) {
			((TileMirror) tile).findTarget();
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ROT_PROP);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileMirror();
	}

	@Override
	public int getParticleTintIndex() {
		return 0;
	}
}
