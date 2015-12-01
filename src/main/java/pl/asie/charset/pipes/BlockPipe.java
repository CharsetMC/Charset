package pl.asie.charset.pipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.refs.Properties;

public class BlockPipe extends BlockContainer {
	public BlockPipe() {
		super(Material.glass);
		setUnlocalizedName("charset.pipe");
		setDefaultState(this.blockState.getBaseState());
		setHardness(0.3F);
	}

	private TilePipe getTilePipe(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TilePipe ? (TilePipe) tileEntity : null;
	}

	private TilePipe removedPipeTile;

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TilePipe tilePipe = getTilePipe(world, pos);

		if (tilePipe != null) {
			return state
					.withProperty(Properties.DOWN, tilePipe.connects(EnumFacing.DOWN))
					.withProperty(Properties.UP, tilePipe.connects(EnumFacing.UP))
					.withProperty(Properties.NORTH, tilePipe.connects(EnumFacing.NORTH))
					.withProperty(Properties.SOUTH, tilePipe.connects(EnumFacing.SOUTH))
					.withProperty(Properties.WEST, tilePipe.connects(EnumFacing.WEST))
					.withProperty(Properties.EAST, tilePipe.connects(EnumFacing.EAST));
		} else {
			return state;
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			removedPipeTile = getTilePipe(world, pos);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(this));

		TilePipe tilePipe = getTilePipe(world, pos);
		if (tilePipe == null && removedPipeTile != null) {
			tilePipe = removedPipeTile;
			if (!tilePipe.getPos().equals(pos) || tilePipe.getWorld() != world) {
				tilePipe = null;
			}

			removedPipeTile = null;
		}

		if (tilePipe != null) {
			for (PipeItem p : tilePipe.getPipeItems()) {
				if (p.isValid()) {
					ret.add(p.getStack());
				}
			}
		}

		return ret;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TilePipe tilePipe = getTilePipe(world, pos);
		if (tilePipe != null) {
			tilePipe.onNeighborBlockChange();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType() {
		return 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TilePipe();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] {
				Properties.DOWN,
				Properties.UP,
				Properties.NORTH,
				Properties.SOUTH,
				Properties.WEST,
				Properties.EAST
		});
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		setBlockBoundsBasedOnState(world, pos);
		return super.getCollisionBoundingBox(world, pos, state);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		setBlockBounds(BoundingBox.getBox(neighbors(world, pos)));
	}

	/**
	 * @author Sangar, Vexatos, asie
	 */
	private static class BoundingBox {
		private static final AxisAlignedBB[] bounds = new AxisAlignedBB[0x40];

		static {
			for (int mask = 0; mask < 0x40; ++mask) {
				bounds[mask] = AxisAlignedBB.fromBounds(
						((mask & (1 << 4)) != 0 ? 0 : 0.25),
						((mask & (1 << 0)) != 0 ? 0 : 0.25),
						((mask & (1 << 2)) != 0 ? 0 : 0.25),
						((mask & (1 << 5)) != 0 ? 1 : 0.75),
						((mask & (1 << 1)) != 0 ? 1 : 0.75),
						((mask & (1 << 3)) != 0 ? 1 : 0.75)
				);
			}
		}

		private static AxisAlignedBB getBox(int msk) {
			return bounds[msk];
		}
	}

	private int neighbors(IBlockAccess world, BlockPos pos) {
		int result = 0;
		TilePipe pipe = getTilePipe(world, pos);
		if (pipe != null) {
			for (EnumFacing side : EnumFacing.VALUES) {
				if (pipe.connects(side)) {
					result |= 1 << side.ordinal();
				}
			}
		}
		return result;
	}

	protected void setBlockBounds(AxisAlignedBB bounds) {
		setBlockBounds((float) bounds.minX, (float) bounds.minY, (float) bounds.minZ, (float) bounds.maxX, (float) bounds.maxY, (float) bounds.maxZ);
	}
}
